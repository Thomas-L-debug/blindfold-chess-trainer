package com.blindfoldchess.trainer.feature.board

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
import androidx.compose.material3.Checkbox
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

private val SideControlsWidth = 72.dp

@Composable
fun BoardPanel(
    showNotation: Boolean,
    onShowNotationChange: (Boolean) -> Unit,
    showArrows: Boolean,
    onShowArrowsChange: (Boolean) -> Unit,
    onHideBoard: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: SquareHighlight? = null,
    arrows: List<BoardArrow> = emptyList(),
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
                modifier = Modifier.size(boardSize),
            )
            Column(
                modifier = Modifier
                    .width(SideControlsWidth)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BoardOption(
                    label = stringResource(R.string.board_show_notation),
                    checked = showNotation,
                    onCheckedChange = onShowNotationChange,
                )
                BoardOption(
                    label = stringResource(R.string.board_show_arrows),
                    checked = showArrows,
                    onCheckedChange = onShowArrowsChange,
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onHideBoard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.board_hide),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardOption(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
