package com.blindfoldchess.trainer.feature.drills

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.feature.board.BoardArrow
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import kotlinx.coroutines.delay

private const val LEGAL_HIGHLIGHT_MS = 500L
private const val ILLEGAL_HIGHLIGHT_MS = 1500L

@Composable
fun FreeBoardScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    onMoveArrows: (List<BoardArrow>) -> Unit = {},
    onPiecesChange: (List<OccupiedSquare>) -> Unit = {},
    onSelectedSquareChange: (Square?) -> Unit = {},
    onSquareClickChange: (((Square) -> Unit)?) -> Unit = {},
    viewModel: FreeBoardViewModel = viewModel(),
) {
    BoardPlayEffects(
        uiState = viewModel.uiState.collectAsState().value,
        viewModel = viewModel,
        onSquareHighlight = onSquareHighlight,
        onMoveArrows = onMoveArrows,
        onPiecesChange = onPiecesChange,
        onSelectedSquareChange = onSelectedSquareChange,
        onSquareClickChange = onSquareClickChange,
    )

    val uiState by viewModel.uiState.collectAsState()
    FreeBoardPlayBody(
        title = stringResource(R.string.drill_free_board_title),
        description = stringResource(R.string.drill_free_board_description),
        uiState = uiState,
        viewModel = viewModel,
        onBack = onBack,
    )
}

@Composable
internal fun BoardPlayEffects(
    uiState: FreeBoardUiState,
    viewModel: FreeBoardViewModel,
    onSquareHighlight: (SquareHighlight?) -> Unit,
    onMoveArrows: (List<BoardArrow>) -> Unit,
    onPiecesChange: (List<OccupiedSquare>) -> Unit,
    onSelectedSquareChange: (Square?) -> Unit,
    onSquareClickChange: (((Square) -> Unit)?) -> Unit,
) {
    val tapHandler = remember<(Square) -> Unit>(viewModel) {
        { square -> viewModel.onSquareTap(square) }
    }

    LaunchedEffect(
        uiState.flashToken,
        uiState.lastPlayedFrom,
        uiState.lastPlayedTo,
        uiState.lastAttemptCorrect,
        uiState.lastAttemptFrom,
        uiState.lastAttemptSquare,
    ) {
        val square = uiState.lastAttemptSquare
        val correct = uiState.lastAttemptCorrect
        val legalFrom = uiState.lastPlayedFrom
        val legalTo = uiState.lastPlayedTo

        fun showLegalArrows() {
            if (legalFrom == null || legalTo == null) {
                onMoveArrows(emptyList())
            } else {
                onMoveArrows(listOf(BoardArrow(legalFrom, legalTo)))
            }
        }

        if (uiState.flashToken == 0 || square == null || correct == null) {
            onSquareHighlight(null)
            showLegalArrows()
            return@LaunchedEffect
        }

        onSquareHighlight(SquareHighlight(square, correct = correct))
        if (!correct) {
            val attemptTo = uiState.lastAttemptSquare ?: square
            val attemptFrom = uiState.lastAttemptFrom ?: attemptTo
            onMoveArrows(listOf(BoardArrow(attemptFrom, attemptTo)))
        } else {
            showLegalArrows()
        }
        delay(if (correct) LEGAL_HIGHLIGHT_MS else ILLEGAL_HIGHLIGHT_MS)
        onSquareHighlight(null)
        showLegalArrows()
    }

    LaunchedEffect(uiState.pieces) {
        onPiecesChange(uiState.pieces)
    }

    LaunchedEffect(uiState.fromSquare) {
        onSelectedSquareChange(uiState.fromSquare)
    }

    LaunchedEffect(uiState.inputEnabled, tapHandler) {
        onSquareClickChange(if (uiState.inputEnabled) tapHandler else null)
    }

    DisposableEffect(Unit) {
        onDispose {
            onPiecesChange(emptyList())
            onSelectedSquareChange(null)
            onSquareClickChange(null)
            onMoveArrows(emptyList())
            onSquareHighlight(null)
        }
    }
}
