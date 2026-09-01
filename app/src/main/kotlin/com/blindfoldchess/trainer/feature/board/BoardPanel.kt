package com.blindfoldchess.trainer.feature.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square

private val SideControlsWidth = 72.dp

@Composable
fun BoardPanel(
    showNotation: Boolean,
    onShowNotationChange: (Boolean) -> Unit,
    showArrows: Boolean,
    onShowArrowsChange: (Boolean) -> Unit,
    showPieces: Boolean,
    onShowPiecesChange: (Boolean) -> Unit,
    onHideBoard: () -> Unit,
    flipped: Boolean,
    onFlipBoard: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: SquareHighlight? = null,
    arrows: List<BoardArrow> = emptyList(),
    pieces: List<OccupiedSquare> = emptyList(),
    selectedSquare: Square? = null,
    onSquareClick: ((Square) -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        val boardSize = (maxWidth - SideControlsWidth).coerceAtLeast(0.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(boardSize),
            verticalAlignment = Alignment.Top,
        ) {
            ChessBoard(
                showNotation = showNotation,
                highlight = highlight,
                showArrows = showArrows,
                arrows = arrows,
                showPieces = showPieces,
                pieces = pieces,
                selectedSquare = selectedSquare,
                onSquareClick = onSquareClick,
                flipped = flipped,
                modifier = Modifier.size(boardSize),
            )
            Column(
                modifier = Modifier
                    .width(SideControlsWidth)
                    .fillMaxHeight()
                    .padding(start = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedButton(
                    onClick = onHideBoard,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = stringResource(R.string.board_hide),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                BoardToggle(
                    label = stringResource(R.string.board_flip),
                    checked = flipped,
                    onCheckedChange = { onFlipBoard() },
                )
                BoardToggle(
                    label = stringResource(R.string.board_show_notation),
                    checked = showNotation,
                    onCheckedChange = onShowNotationChange,
                )
                BoardToggle(
                    label = stringResource(R.string.board_show_arrows),
                    checked = showArrows,
                    onCheckedChange = onShowArrowsChange,
                )
                BoardToggle(
                    label = stringResource(R.string.board_show_pieces),
                    checked = showPieces,
                    onCheckedChange = onShowPiecesChange,
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BoardToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val modifier = Modifier.fillMaxWidth()
    val contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
    if (checked) {
        Button(
            onClick = { onCheckedChange(false) },
            modifier = modifier,
            contentPadding = contentPadding,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            ToggleLabel(label)
        }
    } else {
        OutlinedButton(
            onClick = { onCheckedChange(true) },
            modifier = modifier,
            contentPadding = contentPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            ToggleLabel(label)
        }
    }
}

@Composable
private fun ToggleLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        maxLines = 2,
    )
}
