package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import com.blindfoldchess.trainer.core.chess.ChessSpeechParser
import com.blindfoldchess.trainer.core.chess.PiecePathDrill
import com.blindfoldchess.trainer.core.chess.PieceType
import com.blindfoldchess.trainer.core.chess.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PiecePathUiState(
    val piece: PieceType = PieceType.KNIGHT,
    val start: Square? = null,
    val target: Square? = null,
    val current: Square? = null,
    val pendingFile: Char? = null,
    val path: List<Square> = emptyList(),
    val solved: Boolean = false,
    val illegal: Boolean = false,
    val unrecognized: Boolean = false,
    val lastMoveSquare: Square? = null,
    val lastMoveFrom: Square? = null,
    val lastMoveLegal: Boolean? = null,
    val flashToken: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val lastSpoken: String? = null,
)

class PiecePathDrillViewModel(
    private val drill: PiecePathDrill = PiecePathDrill(),
    initialPiece: PieceType = PieceType.KNIGHT,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PiecePathUiState(piece = initialPiece))
    val uiState: StateFlow<PiecePathUiState> = _uiState.asStateFlow()

    init {
        loadNextPuzzle()
    }

    fun selectPiece(piece: PieceType) {
        if (piece == _uiState.value.piece && _uiState.value.start != null) return
        _uiState.update { it.copy(piece = piece) }
        loadNextPuzzle()
    }

    fun onFile(file: Char) {
        val state = _uiState.value
        if (state.solved) return
        _uiState.update {
            it.copy(pendingFile = file, illegal = false, unrecognized = false, lastSpoken = null)
        }
    }

    fun onRank(rank: Int) {
        val state = _uiState.value
        if (state.solved) return
        val file = state.pendingFile ?: return
        tryMove(Square(file, rank))
    }

    fun playSpoken(utterances: List<String>) {
        val state = _uiState.value
        if (state.solved || state.start == null) return
        val heard = utterances.map { it.trim() }.filter { it.isNotEmpty() }
        if (heard.isEmpty()) return
        for (utterance in heard) {
            val parsed = ChessSpeechParser.parsePathMove(utterance)
            val square = parsed.square ?: continue
            _uiState.update { it.copy(lastSpoken = utterance, unrecognized = false) }
            if (parsed.piece != null && parsed.piece != state.piece) {
                restartFromZero(attempted = square)
                return
            }
            tryMove(square)
            return
        }
        _uiState.update {
            it.copy(
                lastSpoken = heard.first(),
                unrecognized = true,
                illegal = false,
            )
        }
    }

    private fun tryMove(to: Square) {
        val state = _uiState.value
        val from = state.current ?: return
        if (!drill.isLegalMove(state.piece, from, to)) {
            restartFromZero(attempted = to)
            return
        }
        val reachedTarget = to == state.target
        _uiState.update {
            it.copy(
                current = to,
                pendingFile = null,
                path = it.path + to,
                solved = reachedTarget,
                illegal = false,
                unrecognized = false,
                lastMoveSquare = to,
                lastMoveFrom = from,
                lastMoveLegal = true,
                flashToken = it.flashToken + 1,
                correctCount = it.correctCount + if (reachedTarget) 1 else 0,
                totalCount = it.totalCount + if (reachedTarget) 1 else 0,
                lastSpoken = null,
            )
        }
    }

    fun loadNextPuzzle() {
        val piece = _uiState.value.piece
        val puzzle = drill.nextPuzzle(piece)
        _uiState.update {
            it.copy(
                piece = puzzle.piece,
                start = puzzle.start,
                target = puzzle.target,
                current = puzzle.start,
                pendingFile = null,
                path = emptyList(),
                solved = false,
                illegal = false,
                unrecognized = false,
                lastMoveSquare = null,
                lastMoveFrom = null,
                lastMoveLegal = null,
                flashToken = it.flashToken + 1,
                lastSpoken = null,
            )
        }
    }

    private fun restartFromZero(attempted: Square) {
        val start = _uiState.value.start ?: return
        val from = _uiState.value.current ?: start
        _uiState.update {
            it.copy(
                current = start,
                pendingFile = null,
                path = emptyList(),
                solved = false,
                illegal = true,
                unrecognized = false,
                lastMoveSquare = attempted,
                lastMoveFrom = from,
                lastMoveLegal = false,
                flashToken = it.flashToken + 1,
                totalCount = it.totalCount + 1,
            )
        }
    }
}
