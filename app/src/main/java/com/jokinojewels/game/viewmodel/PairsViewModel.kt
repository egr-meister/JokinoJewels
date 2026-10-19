package com.jokinojewels.game.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.jokinojewels.game.audio.Sfx
import com.jokinojewels.game.audio.SoundManager
import com.jokinojewels.game.data.GameRepository
import com.jokinojewels.game.logic.GameLogic
import com.jokinojewels.game.logic.LevelConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PairsOutcome { WIN, LOSE }

data class PairsCardUi(
    val index: Int,
    val itemIndex: Int,
    val isFaceUp: Boolean,
    val isMatched: Boolean
)

data class PairsUiState(
    val level: Int = 0,
    val isValid: Boolean = true,
    val isLocked: Boolean = false,
    val isReady: Boolean = false,
    val columns: Int = 2,
    val totalPairs: Int = 0,
    val cards: List<PairsCardUi> = emptyList(),
    val matchedPairs: Int = 0,
    val secondsLeft: Int = 0,
    val totalSeconds: Int = 0,
    val isPaused: Boolean = false,
    val outcome: PairsOutcome? = null
) {
    val timeFraction: Float
        get() = if (totalSeconds <= 0) 0f else (secondsLeft.toFloat() / totalSeconds).coerceIn(0f, 1f)
}

/**
 * One Matching Pairs run. Deck, matched cards and remaining time are kept in
 * [SavedStateHandle], so process recreation restores the board (paused).
 */
class PairsViewModel(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val savedState: SavedStateHandle,
    private val level: Int
) : ViewModel() {

    private val config: LevelConfig? = GameLogic.levelConfig(level)

    private var deck: List<Int> = emptyList()
    private val faceUp = mutableListOf<Int>()
    private val matched = mutableSetOf<Int>()
    private var remainingMs: Long = 0L
    private var inputLocked = false
    private var finishing = false

    private var timerJob: Job? = null
    private var mismatchJob: Job? = null

    private val _state = MutableStateFlow(
        PairsUiState(
            level = level,
            isValid = config != null,
            columns = config?.columns ?: 2,
            totalPairs = config?.pairs ?: 0,
            totalSeconds = config?.seconds ?: 0,
            secondsLeft = config?.seconds ?: 0
        )
    )
    val state: StateFlow<PairsUiState> = _state.asStateFlow()

    init {
        val cfg = config
        if (cfg != null) {
            viewModelScope.launch {
                val data = repository.gameData.first()
                if (!GameLogic.isLevelUnlocked(cfg.level, data.pairsBest)) {
                    _state.update { it.copy(isLocked = true) }
                    return@launch
                }
                val restored = restore(cfg)
                if (!restored) newRun(cfg)
                _state.update { it.copy(isReady = true, isPaused = restored) }
                publish()
                startTimer()
            }
        }
    }

    fun onCardTap(index: Int) {
        val cfg = config ?: return
        val s = _state.value
        if (!s.isReady || s.isPaused || s.outcome != null || finishing || inputLocked) return
        if (index !in deck.indices || index in matched || index in faceUp) return

        faceUp.add(index)
        soundManager.play(Sfx.CLICK)
        if (faceUp.size < 2) {
            publish()
            return
        }

        val first = faceUp[0]
        val second = faceUp[1]
        if (deck[first] == deck[second]) {
            matched.add(first)
            matched.add(second)
            faceUp.clear()
            soundManager.play(Sfx.MATCH)
            publish()
            if (GameLogic.isWin(matched.size / 2, cfg.pairs)) finish(PairsOutcome.WIN)
        } else {
            inputLocked = true
            publish()
            mismatchJob = viewModelScope.launch {
                pausableDelay(GameLogic.MISMATCH_DELAY_MS)
                faceUp.clear()
                inputLocked = false
                publish()
            }
        }
    }

    fun pause() {
        _state.update {
            if (!it.isReady || it.outcome != null || finishing) it else it.copy(isPaused = true)
        }
    }

    fun resume() {
        _state.update { it.copy(isPaused = false) }
    }

    fun restart() {
        val cfg = config ?: return
        if (!_state.value.isReady || finishing) return
        timerJob?.cancel()
        mismatchJob?.cancel()
        newRun(cfg)
        _state.update { it.copy(isPaused = false, outcome = null) }
        publish()
        startTimer()
    }

    private fun newRun(cfg: LevelConfig) {
        deck = GameLogic.buildDeck(cfg.pairs)
        faceUp.clear()
        matched.clear()
        inputLocked = false
        finishing = false
        remainingMs = cfg.seconds * 1000L
    }

    private fun restore(cfg: LevelConfig): Boolean {
        val savedDeck = savedState.get<IntArray>(KEY_DECK)?.toList() ?: return false
        if (!GameLogic.isValidDeck(savedDeck, cfg.pairs)) return false
        val savedMs = savedState.get<Long>(KEY_REMAINING) ?: return false
        if (savedMs <= 0L) return false
        deck = savedDeck
        matched.clear()
        val savedMatched = savedState.get<IntArray>(KEY_MATCHED)?.filter { it in deck.indices }.orEmpty()
        // Only keep complete, genuinely matching pairs.
        savedMatched.groupBy { deck[it] }.values.filter { it.size == 2 }.forEach { matched.addAll(it) }
        faceUp.clear()
        inputLocked = false
        finishing = false
        remainingMs = savedMs.coerceAtMost(cfg.seconds * 1000L)
        return !GameLogic.isWin(matched.size / 2, cfg.pairs)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (remainingMs > 0L) {
                delay(TICK_MS)
                val s = _state.value
                if (!s.isPaused && s.outcome == null && !finishing) {
                    remainingMs = (remainingMs - TICK_MS).coerceAtLeast(0L)
                    publishTime()
                }
            }
            if (!finishing && _state.value.outcome == null) finish(PairsOutcome.LOSE)
        }
    }

    private fun finish(outcome: PairsOutcome) {
        if (finishing) return
        finishing = true
        timerJob?.cancel()
        mismatchJob?.cancel()
        inputLocked = true
        soundManager.play(if (outcome == PairsOutcome.WIN) Sfx.WIN else Sfx.LOSE)
        val matchedPairs = matched.size / 2
        viewModelScope.launch {
            // Persist BEFORE signalling the UI, so navigation can never cancel the save.
            withContext(NonCancellable) { repository.savePairsResult(level, matchedPairs) }
            savedState.remove<IntArray>(KEY_DECK)
            savedState.remove<IntArray>(KEY_MATCHED)
            savedState.remove<Long>(KEY_REMAINING)
            _state.update { it.copy(outcome = outcome, isPaused = false) }
        }
    }

    private fun publish() {
        val cfg = config ?: return
        val cards = deck.mapIndexed { i, item ->
            PairsCardUi(
                index = i,
                itemIndex = item,
                isFaceUp = i in faceUp || i in matched,
                isMatched = i in matched
            )
        }
        _state.update {
            it.copy(
                cards = cards,
                columns = cfg.columns,
                totalPairs = cfg.pairs,
                matchedPairs = matched.size / 2,
                secondsLeft = GameLogic.secondsLeft(remainingMs),
                totalSeconds = cfg.seconds
            )
        }
        persist()
    }

    private fun publishTime() {
        val seconds = GameLogic.secondsLeft(remainingMs)
        if (seconds != _state.value.secondsLeft) {
            _state.update { it.copy(secondsLeft = seconds) }
        }
        savedState[KEY_REMAINING] = remainingMs
    }

    private fun persist() {
        if (finishing) return
        savedState[KEY_DECK] = deck.toIntArray()
        savedState[KEY_MATCHED] = matched.toIntArray()
        savedState[KEY_REMAINING] = remainingMs
    }

    private suspend fun pausableDelay(totalMs: Long) {
        var remaining = totalMs
        while (remaining > 0) {
            delay(MISMATCH_TICK_MS)
            if (!_state.value.isPaused) remaining -= MISMATCH_TICK_MS
        }
    }

    class Factory(
        private val repository: GameRepository,
        private val soundManager: SoundManager,
        private val level: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(PairsViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return PairsViewModel(repository, soundManager, extras.createSavedStateHandle(), level) as T
        }
    }

    private companion object {
        const val KEY_DECK = "pairs_deck"
        const val KEY_MATCHED = "pairs_matched"
        const val KEY_REMAINING = "pairs_remaining_ms"
        const val TICK_MS = 200L
        const val MISMATCH_TICK_MS = 50L
    }
}
