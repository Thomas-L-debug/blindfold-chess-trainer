package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import com.blindfoldchess.trainer.core.chess.FindSquareDrill
import com.blindfoldchess.trainer.core.chess.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FindSquareUiState(
    val target: Square? = null,
    val answered: Boolean = false,
    val wasCorrect: Boolean? = null,
    val lastAttempt: Square? = null,
    val flashToken: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
)

class FindSquareViewModel(
    private val drill: FindSquareDrill = FindSquareDrill(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindSquareUiState())
    val uiState: StateFlow<FindSquareUiState> = _uiState.asStateFlow()

    init {
        loadNextQuestion()
    }

    fun onSquareTap(square: Square) {
        val state = _uiState.value
        val target = state.target ?: return
        if (state.answered) return
        val correct = drill.isCorrect(target, square)
        _uiState.update {
            it.copy(
                answered = true,
                wasCorrect = correct,
                lastAttempt = square,
                flashToken = it.flashToken + 1,
                correctCount = it.correctCount + if (correct) 1 else 0,
                totalCount = it.totalCount + 1,
            )
        }
    }

    fun unlockRetry() {
        val state = _uiState.value
        if (state.wasCorrect != false) return
        _uiState.update {
            it.copy(
                answered = false,
                wasCorrect = null,
                lastAttempt = null,
            )
        }
    }

    fun loadNextQuestion() {
        _uiState.update {
            it.copy(
                target = drill.nextQuestion(avoid = it.target),
                answered = false,
                wasCorrect = null,
                lastAttempt = null,
                flashToken = it.flashToken + 1,
            )
        }
    }
}
