package com.blindfoldchess.trainer.feature.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square

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
    highlights: List<SquareHighlight> = emptyList(),
    arrows: List<BoardArrow> = emptyList(),
    pieces: List<OccupiedSquare> = emptyList(),
    selectedSquare: Square? = null,
    onSquareClick: ((Square) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ChessBoard(
            showNotation = showNotation,
            highlights = highlights,
            showArrows = showArrows,
            arrows = arrows,
            showPieces = showPieces,
            pieces = pieces,
            selectedSquare = selectedSquare,
            onSquareClick = onSquareClick,
            flipped = flipped,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedButton(
                onClick = onHideBoard,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
            ) {
                ToggleLabel(stringResource(R.string.board_hide))
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
        }
    }
}

@Composable
private fun RowScope.BoardToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val modifier = Modifier.weight(1f)
    val contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
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
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.sp,
        ),
        textAlign = TextAlign.Center,
        maxLines = 2,
        softWrap = true,
    )
}
