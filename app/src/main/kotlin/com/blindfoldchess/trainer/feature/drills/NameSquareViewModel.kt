package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import com.blindfoldchess.trainer.core.chess.ChessSpeechParser
import com.blindfoldchess.trainer.core.chess.FindSquareDrill
import com.blindfoldchess.trainer.core.chess.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NameSquareUiState(
    val target: Square? = null,
    val pendingFile: Char? = null,
    val answered: Boolean = false,
    val wasCorrect: Boolean? = null,
    val lastAttempt: Square? = null,
    val unrecognized: Boolean = false,
    val lastSpoken: String? = null,
    val flashToken: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
)

class NameSquareViewModel(
    private val drill: FindSquareDrill = FindSquareDrill(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(NameSquareUiState())
    val uiState: StateFlow<NameSquareUiState> = _uiState.asStateFlow()

    init {
        loadNextQuestion()
    }

    fun onFile(file: Char) {
        if (_uiState.value.answered) return
        _uiState.update {
            it.copy(pendingFile = file, unrecognized = false, lastSpoken = null)
        }
    }

    fun onRank(rank: Int) {
        val state = _uiState.value
        if (state.answered) return
        val file = state.pendingFile ?: return
        submit(Square(file, rank))
    }

    fun playSpoken(utterances: List<String>) {
        val state = _uiState.value
        if (state.answered) return
        val heard = utterances.map { it.trim() }.filter { it.isNotEmpty() }
        if (heard.isEmpty()) return
        for (utterance in heard) {
            val square = ChessSpeechParser.parsePathMove(utterance).square ?: continue
            submit(square, spoken = utterance)
            return
        }
        _uiState.update {
            it.copy(
                lastSpoken = heard.first(),
                unrecognized = true,
                pendingFile = null,
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
                pendingFile = null,
                unrecognized = false,
                lastSpoken = null,
            )
        }
    }

    fun loadNextQuestion() {
        _uiState.update {
            it.copy(
                target = drill.nextQuestion(avoid = it.target),
                pendingFile = null,
                answered = false,
                wasCorrect = null,
                lastAttempt = null,
                unrecognized = false,
                lastSpoken = null,
                flashToken = it.flashToken + 1,
            )
        }
    }

    private fun submit(square: Square, spoken: String? = null) {
        val state = _uiState.value
        val target = state.target ?: return
        if (state.answered) return
        val correct = drill.isCorrect(target, square)
        _uiState.update {
            it.copy(
                answered = true,
                wasCorrect = correct,
                lastAttempt = square,
                pendingFile = null,
                unrecognized = false,
                lastSpoken = spoken,
                flashToken = it.flashToken + 1,
                correctCount = it.correctCount + if (correct) 1 else 0,
                totalCount = it.totalCount + 1,
            )
        }
    }
}
