package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import com.blindfoldchess.trainer.core.chess.SquareColor
import com.blindfoldchess.trainer.core.chess.SquareColorDrill
import com.blindfoldchess.trainer.core.chess.SquareColorQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SquareColorDrillUiState(
    val question: SquareColorQuestion? = null,
    val answered: Boolean = false,
    val wasCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
)

class SquareColorDrillViewModel(
    private val drill: SquareColorDrill = SquareColorDrill(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(SquareColorDrillUiState())
    val uiState: StateFlow<SquareColorDrillUiState> = _uiState.asStateFlow()

    init {
        loadNextQuestion()
    }

    fun onAnswer(answer: SquareColor) {
        val question = _uiState.value.question ?: return
        if (_uiState.value.answered) return

        val correct = drill.isCorrect(question, answer)
        _uiState.update { state ->
            state.copy(
                answered = true,
                wasCorrect = correct,
                correctCount = state.correctCount + if (correct) 1 else 0,
                totalCount = state.totalCount + 1,
            )
        }
    }

    fun loadNextQuestion() {
        _uiState.update {
            it.copy(
                question = drill.nextQuestion(),
                answered = false,
                wasCorrect = null,
            )
        }
    }
}