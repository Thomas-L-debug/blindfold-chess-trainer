package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

private const val SQUARE_HIGHLIGHT_MS = 500L

private val FILE_ROWS = listOf(
    listOf('a', 'e'),
    listOf('b', 'f'),
    listOf('c', 'g'),
    listOf('d', 'h'),
)

private val RANK_ROWS = listOf(
    listOf(1, 5),
    listOf(2, 6),
    listOf(3, 7),
    listOf(4, 8),
)

@Composable
fun PiecePathDrillScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    onMoveArrows: (List<BoardArrow>) -> Unit = {},
    viewModel: PiecePathDrillViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.start, uiState.path) {
        onMoveArrows(arrowsAlong(uiState.start, uiState.path))
    }

    LaunchedEffect(uiState.flashToken) {
        val square = uiState.lastMoveSquare
        val legal = uiState.lastMoveLegal
        if (uiState.flashToken == 0 || square == null || legal == null) {
            onSquareHighlight(null)
            return@LaunchedEffect
        }
        onSquareHighlight(SquareHighlight(square, correct = legal))
        delay(SQUARE_HIGHLIGHT_MS)
        onSquareHighlight(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.drill_piece_path_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.drill_piece_path_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
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

        val pathStart = uiState.start
        if (uiState.path.isNotEmpty() && pathStart != null) {
            val trail = (listOf(pathStart) + uiState.path)
                .joinToString(" → ") { it.algebraic }
            Text(
                text = trail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.solved -> {
                Text(
                    text = stringResource(R.string.feedback_correct),
                    style = MaterialTheme.typography.titleMedium,
                    color = Correct,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = viewModel::loadNextPuzzle) {
                    Text(stringResource(R.string.next_question))
                }
            }
            uiState.illegal -> {
                Text(
                    text = stringResource(R.string.feedback_illegal_move),
                    style = MaterialTheme.typography.titleMedium,
                    color = Incorrect,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (!uiState.solved) {
            Spacer(modifier = Modifier.height(16.dp))
            CoordinatePad(
                pendingFile = uiState.pendingFile,
                enabled = uiState.start != null,
                onFile = viewModel::onFile,
                onRank = viewModel::onRank,
            )
        }

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

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            Text("Back")
        }

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

@Composable
private fun CoordinatePad(
    pendingFile: Char?,
    enabled: Boolean,
    onFile: (Char) -> Unit,
    onRank: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FILE_ROWS.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { file ->
                        CoordinateButton(
                            label = file.toString(),
                            selected = pendingFile == file,
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            onClick = { onFile(file) },
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RANK_ROWS.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { rank ->
                        CoordinateButton(
                            label = rank.toString(),
                            selected = false,
                            enabled = enabled && pendingFile != null,
                            modifier = Modifier.weight(1f),
                            onClick = { onRank(rank) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinateButton(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier = modifier.heightIn(min = 48.dp)
    if (selected) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = buttonModifier,
        ) {
            Text(label)
        }
    }
}

private fun pieceLabel(piece: PieceType): Int = when (piece) {
    PieceType.BISHOP -> R.string.piece_bishop
    PieceType.KNIGHT -> R.string.piece_knight
    PieceType.ROOK -> R.string.piece_rook
    PieceType.QUEEN -> R.string.piece_queen
}
