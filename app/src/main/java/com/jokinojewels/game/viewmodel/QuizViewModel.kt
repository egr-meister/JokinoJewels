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
import com.jokinojewels.game.data.QuizCategory
import com.jokinojewels.game.data.QuizContent
import com.jokinojewels.game.data.QuizQuestion
import com.jokinojewels.game.logic.GameLogic
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuizUiState(
    val isValid: Boolean = true,
    val categoryId: String = "",
    val categoryTitle: String = "",
    val questionIndex: Int = 0,
    val total: Int = 0,
    val question: QuizQuestion? = null,
    val selectedIndex: Int? = null,
    val score: Int = 0,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false
) {
    val isRevealed: Boolean get() = selectedIndex != null
    val progressLabel: String get() = "${(questionIndex + 1).coerceAtMost(total)}/$total"
}

/**
 * One quiz run. Survives configuration changes (ViewModel) and process recreation
 * (question index + committed score live in [SavedStateHandle]).
 */
class QuizViewModel(
    private val repository: GameRepository,
    private val soundManager: SoundManager,
    private val savedState: SavedStateHandle,
    categoryId: String
) : ViewModel() {

    private val category: QuizCategory? = QuizContent.category(categoryId)
        ?.takeIf { it.questions.isNotEmpty() }

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var advanceJob: Job? = null

    private fun initialState(): QuizUiState {
        val cat = category ?: return QuizUiState(isValid = false)
        val total = cat.questions.size
        val index = (savedState.get<Int>(KEY_INDEX) ?: 0).coerceIn(0, total - 1)
        val score = (savedState.get<Int>(KEY_SCORE) ?: 0).coerceIn(0, index)
        val restoredMidRun = index > 0
        return QuizUiState(
            isValid = true,
            categoryId = cat.id,
            categoryTitle = cat.title,
            questionIndex = index,
            total = total,
            question = cat.questions[index],
            score = score,
            isPaused = restoredMidRun
        )
    }

    fun answer(optionIndex: Int) {
        val s = _state.value
        val question = s.question ?: return
        if (!s.isValid || s.isPaused || s.isFinished || s.isRevealed) return
        if (optionIndex !in question.options.indices) return
        val correct = optionIndex == question.correctIndex
        soundManager.play(if (correct) Sfx.CORRECT else Sfx.WRONG)
        _state.update { it.copy(selectedIndex = optionIndex) }
        advanceJob = viewModelScope.launch {
            pausableDelay(GameLogic.ANSWER_REVEAL_MS)
            advance(correct)
        }
    }

    fun pause() {
        _state.update { if (it.isFinished || !it.isValid) it else it.copy(isPaused = true) }
    }

    fun resume() {
        _state.update { it.copy(isPaused = false) }
    }

    fun restart() {
        advanceJob?.cancel()
        advanceJob = null
        savedState[KEY_INDEX] = 0
        savedState[KEY_SCORE] = 0
        val cat = category ?: return
        _state.value = QuizUiState(
            isValid = true,
            categoryId = cat.id,
            categoryTitle = cat.title,
            questionIndex = 0,
            total = cat.questions.size,
            question = cat.questions.first(),
            score = 0
        )
    }

    private suspend fun advance(correct: Boolean) {
        val cat = category ?: return
        val s = _state.value
        val newScore = s.score + if (correct) 1 else 0
        val next = s.questionIndex + 1
        if (next >= s.total) {
            // Persist BEFORE signalling the UI, so navigation can never cancel the save.
            withContext(NonCancellable) { repository.saveQuizScore(cat.id, newScore) }
            savedState[KEY_INDEX] = 0
            savedState[KEY_SCORE] = 0
            _state.update { it.copy(score = newScore, isFinished = true, isPaused = false) }
        } else {
            savedState[KEY_INDEX] = next
            savedState[KEY_SCORE] = newScore
            _state.update {
                it.copy(
                    questionIndex = next,
                    question = cat.questions[next],
                    selectedIndex = null,
                    score = newScore
                )
            }
        }
    }

    /** A delay that stands still while the pause overlay is open. */
    private suspend fun pausableDelay(totalMs: Long) {
        var remaining = totalMs
        while (remaining > 0) {
            delay(TICK_MS)
            if (!_state.value.isPaused) remaining -= TICK_MS
        }
    }

    class Factory(
        private val repository: GameRepository,
        private val soundManager: SoundManager,
        private val categoryId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return QuizViewModel(repository, soundManager, extras.createSavedStateHandle(), categoryId) as T
        }
    }

    private companion object {
        const val KEY_INDEX = "quiz_index"
        const val KEY_SCORE = "quiz_score"
        const val TICK_MS = 50L
    }
}
