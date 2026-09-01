package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import com.blindfoldchess.trainer.core.chess.FamousGame
import com.blindfoldchess.trainer.core.chess.GameFollowDrill
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.ReplayMove
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.core.chess.formatPlayedMoves
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FamousGamesUiState(
    val browsing: Boolean = true,
    val games: List<FamousGame> = emptyList(),
    val game: FamousGame? = null,
    val moves: List<ReplayMove> = emptyList(),
    val positions: List<List<OccupiedSquare>> = emptyList(),
    val plyIndex: Int = 0,
    val maxProgressPly: Int = 0,
    val fromSquare: Square? = null,
    val lastAttemptCorrect: Boolean? = null,
    val lastAttemptFrom: Square? = null,
    val lastAttemptSquare: Square? = null,
    val flashToken: Int = 0,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val attemptsOnCurrent: Int = 0,
) {
    val currentMove: ReplayMove?
        get() = moves.getOrNull(plyIndex)

    val lastMove: ReplayMove?
        get() = moves.getOrNull(plyIndex - 1)

    val lastPlayedFrom: Square?
        get() = lastMove?.from

    val lastPlayedTo: Square?
        get() = lastMove?.to

    val pieces: List<OccupiedSquare>
        get() = if (browsing) emptyList() else positions.getOrElse(plyIndex) { emptyList() }

    val playedMoves: String
        get() = formatPlayedMoves(moves, plyIndex)

    val completed: Boolean
        get() = !browsing && moves.isNotEmpty() && maxProgressPly >= moves.size

    val atFrontier: Boolean
        get() = !browsing && plyIndex == maxProgressPly

    val boardTappable: Boolean
        get() = atFrontier && currentMove != null

    val reviewing: Boolean
        get() = !browsing && plyIndex < maxProgressPly

    val canStepBack: Boolean
        get() = !browsing && plyIndex > 0

    val canStepForward: Boolean
        get() = !browsing && plyIndex < maxProgressPly

    val canGoToStart: Boolean
        get() = canStepBack

    val canGoToLatest: Boolean
        get() = canStepForward
}

class FamousGamesViewModel(
    private val drill: GameFollowDrill = GameFollowDrill(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FamousGamesUiState(games = drill.library()),
    )
    val uiState: StateFlow<FamousGamesUiState> = _uiState.asStateFlow()

    fun selectGame(id: String) {
        val loaded = drill.load(id)
        _uiState.update {
            it.copy(
                browsing = false,
                game = loaded.game,
                moves = loaded.moves,
                positions = loaded.positions,
                plyIndex = 0,
                maxProgressPly = 0,
                fromSquare = null,
                lastAttemptCorrect = null,
                lastAttemptFrom = null,
                lastAttemptSquare = null,
                flashToken = it.flashToken + 1,
                correctCount = 0,
                totalCount = 0,
                attemptsOnCurrent = 0,
            )
        }
    }

    fun backToLibrary() {
        _uiState.update {
            FamousGamesUiState(
                browsing = true,
                games = drill.library(),
                flashToken = it.flashToken + 1,
            )
        }
    }

    fun replay() {
        val id = _uiState.value.game?.id ?: return
        selectGame(id)
    }

    fun stepBack() {
        val state = _uiState.value
        if (!state.canStepBack) return
        goToPly(state.plyIndex - 1)
    }

    fun stepForward() {
        val state = _uiState.value
        if (!state.canStepForward) return
        goToPly(state.plyIndex + 1)
    }

    fun goToStart() {
        val state = _uiState.value
        if (!state.canGoToStart) return
        goToPly(0)
    }

    fun goToLatest() {
        val state = _uiState.value
        if (!state.canGoToLatest) return
        goToPly(state.maxProgressPly)
    }

    fun onSquareTap(square: Square) {
        val state = _uiState.value
        if (!state.boardTappable) return
        val expected = state.currentMove ?: return
        val occupant = state.pieces.firstOrNull { it.square == square }
        val selected = state.fromSquare

        if (selected == null) {
            if (occupant != null && occupant.isWhite == expected.isWhite) {
                _uiState.update {
                    it.copy(fromSquare = square, lastAttemptCorrect = null)
                }
            }
            return
        }

        if (square == selected) {
            _uiState.update { it.copy(fromSquare = null, lastAttemptCorrect = null) }
            return
        }

        if (occupant != null && occupant.isWhite == expected.isWhite) {
            _uiState.update {
                it.copy(fromSquare = square, lastAttemptCorrect = null)
            }
            return
        }

        submitMove(from = selected, to = square)
    }

    private fun goToPly(ply: Int) {
        _uiState.update {
            it.copy(
                plyIndex = ply,
                fromSquare = null,
                lastAttemptCorrect = null,
                lastAttemptFrom = null,
                lastAttemptSquare = null,
                flashToken = it.flashToken + 1,
            )
        }
    }

    private fun submitMove(from: Square, to: Square) {
        val state = _uiState.value
        val expected = state.currentMove ?: return
        if (drill.isExpected(expected, from, to)) {
            val nextPly = state.plyIndex + 1
            _uiState.update {
                it.copy(
                    plyIndex = nextPly,
                    maxProgressPly = maxOf(it.maxProgressPly, nextPly),
                    fromSquare = null,
                    lastAttemptCorrect = true,
                    lastAttemptFrom = from,
                    lastAttemptSquare = to,
                    flashToken = it.flashToken + 1,
                    correctCount = it.correctCount + if (it.attemptsOnCurrent == 0) 1 else 0,
                    totalCount = it.totalCount + 1,
                    attemptsOnCurrent = 0,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    fromSquare = null,
                    lastAttemptCorrect = false,
                    lastAttemptFrom = from,
                    lastAttemptSquare = to,
                    flashToken = it.flashToken + 1,
                    attemptsOnCurrent = it.attemptsOnCurrent + 1,
                )
            }
        }
    }
}
