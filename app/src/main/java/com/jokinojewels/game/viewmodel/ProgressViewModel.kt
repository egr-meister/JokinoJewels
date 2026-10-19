package com.jokinojewels.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jokinojewels.game.data.GameData
import com.jokinojewels.game.data.GameRepository
import com.jokinojewels.game.data.QuizContent
import com.jokinojewels.game.logic.GameLogic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryProgressUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val difficulty: Int,
    val best: Int?,
    val total: Int
)

data class LevelProgressUi(
    val level: Int,
    val pairs: Int,
    val columns: Int,
    val seconds: Int,
    val unlocked: Boolean,
    val cleared: Boolean,
    val bestMatched: Int
)

/** Immutable read model shared by Main, Menu, Quiz categories, Levels, Summary and Settings. */
data class ProgressUiState(
    val isLoaded: Boolean = false,
    val categories: List<CategoryProgressUi> = emptyList(),
    val levels: List<LevelProgressUi> = emptyList(),
    val levelsCleared: Int = 0,
    val totalLevels: Int = GameLogic.levelCount,
    val soundEnabled: Boolean = true
)

class ProgressViewModel(private val repository: GameRepository) : ViewModel() {

    val state: StateFlow<ProgressUiState> = repository.gameData
        .map { toUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun resetProgress() {
        viewModelScope.launch { repository.resetProgress() }
    }

    companion object {
        fun toUiState(data: GameData): ProgressUiState = ProgressUiState(
            isLoaded = true,
            categories = QuizContent.categories.map { category ->
                CategoryProgressUi(
                    id = category.id,
                    title = category.title,
                    subtitle = category.subtitle,
                    difficulty = category.difficulty,
                    best = data.quizBest[category.id]?.coerceIn(0, category.questions.size),
                    total = category.questions.size
                )
            },
            levels = GameLogic.levels.map { config ->
                LevelProgressUi(
                    level = config.level,
                    pairs = config.pairs,
                    columns = config.columns,
                    seconds = config.seconds,
                    unlocked = GameLogic.isLevelUnlocked(config.level, data.pairsBest),
                    cleared = GameLogic.isLevelCleared(config.level, data.pairsBest),
                    bestMatched = GameLogic.bestMatched(config.level, data.pairsBest).coerceAtMost(config.pairs)
                )
            },
            levelsCleared = GameLogic.completedLevelCount(data.pairsBest),
            totalLevels = GameLogic.levelCount,
            soundEnabled = data.settings.soundEnabled
        )
    }

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return ProgressViewModel(repository) as T
        }
    }
}
