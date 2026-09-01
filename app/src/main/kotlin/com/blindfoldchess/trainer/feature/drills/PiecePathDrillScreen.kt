package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.PieceType
import com.blindfoldchess.trainer.feature.board.BoardArrow
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.feature.board.arrowsAlong
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlinx.coroutines.delay

private const val LEGAL_HIGHLIGHT_MS = 500L
private const val ILLEGAL_HIGHLIGHT_MS = 1500L

@Composable
fun PiecePathDrillScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    onMoveArrows: (List<BoardArrow>) -> Unit = {},
    viewModel: PiecePathDrillViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(
        uiState.flashToken,
        uiState.start,
        uiState.path,
        uiState.lastMoveLegal,
        uiState.lastMoveSquare,
        uiState.lastMoveFrom,
    ) {
        val dest = uiState.lastMoveSquare
        val legal = uiState.lastMoveLegal
        val from = uiState.lastMoveFrom

        fun showPathArrows() {
            onMoveArrows(arrowsAlong(uiState.start, uiState.path))
        }

        if (uiState.flashToken == 0 || dest == null || legal == null) {
            onSquareHighlight(null)
            showPathArrows()
            return@LaunchedEffect
        }

        onSquareHighlight(SquareHighlight(dest, correct = legal))
        if (!legal) {
            onMoveArrows(listOf(BoardArrow(from ?: dest, dest)))
        } else {
            showPathArrows()
        }
        delay(if (legal) LEGAL_HIGHLIGHT_MS else ILLEGAL_HIGHLIGHT_MS)
        onSquareHighlight(null)
        showPathArrows()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = stringResource(R.string.drill_piece_path_title),
            description = stringResource(R.string.drill_piece_path_description),
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(12.dp))

        PieceSelector(
            selected = uiState.piece,
            enabled = !uiState.solved,
            onSelect = viewModel::selectPiece,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val start = uiState.start?.algebraic.orEmpty()
        val target = uiState.target?.algebraic.orEmpty()
        Text(
            text = stringResource(R.string.piece_path_from_to, start, target),
            fontSize = 32.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(
                R.string.piece_path_current,
                uiState.current?.algebraic.orEmpty(),
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        var showIllegal by remember { mutableStateOf(false) }
        LaunchedEffect(uiState.flashToken, uiState.illegal) {
            if (uiState.illegal) {
                showIllegal = true
                delay(ILLEGAL_HIGHLIGHT_MS)
                showIllegal = false
            } else {
                showIllegal = false
            }
        }

        val pathStart = uiState.start
        val trail = if (uiState.path.isNotEmpty() && pathStart != null) {
            (listOf(pathStart) + uiState.path).joinToString(" → ") { it.algebraic }
        } else {
            " "
        }
        Text(
            text = trail,
            style = MaterialTheme.typography.bodySmall,
            color = if (trail.isBlank()) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        val feedback = when {
            uiState.solved -> stringResource(R.string.feedback_correct)
            showIllegal -> stringResource(R.string.feedback_illegal_move)
            else -> " "
        }
        Text(
            text = feedback,
            style = MaterialTheme.typography.titleMedium,
            color = when {
                uiState.solved -> Correct
                showIllegal -> Incorrect
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = viewModel::loadNextPuzzle,
            enabled = uiState.solved,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.next_question))
        }

        Spacer(modifier = Modifier.height(16.dp))
        CoordinatePad(
            pendingFile = uiState.pendingFile,
            enabled = uiState.start != null && !uiState.solved,
            onFile = viewModel::onFile,
            onRank = viewModel::onRank,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(
                R.string.score_label,
                uiState.correctCount,
                uiState.totalCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PieceSelector(
    selected: PieceType,
    enabled: Boolean,
    onSelect: (PieceType) -> Unit,
) {
    val rows = listOf(
        listOf(PieceType.BISHOP, PieceType.KNIGHT),
        listOf(PieceType.ROOK, PieceType.QUEEN),
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { piece ->
                    FilterChip(
                        selected = piece == selected,
                        onClick = { onSelect(piece) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                text = stringResource(pieceLabel(piece)),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        },
                    )
                }
            }
        }
    }
}

private fun pieceLabel(piece: PieceType): Int = when (piece) {
    PieceType.BISHOP -> R.string.piece_bishop
    PieceType.KNIGHT -> R.string.piece_knight
    PieceType.ROOK -> R.string.piece_rook
    PieceType.QUEEN -> R.string.piece_queen
}
