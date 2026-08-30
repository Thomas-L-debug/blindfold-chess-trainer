package com.blindfoldchess.trainer.feature.board

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R

@Composable
fun AppShell(
    isBoardVisible: Boolean,
    onToggleBoardVisible: () -> Unit,
    showNotation: Boolean,
    onShowNotationChange: (Boolean) -> Unit,
    showArrows: Boolean,
    onShowArrowsChange: (Boolean) -> Unit,
    highlight: SquareHighlight? = null,
    arrows: List<BoardArrow> = emptyList(),
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        if (isBoardVisible) {
            BoardPanel(
                showNotation = showNotation,
                onShowNotationChange = onShowNotationChange,
                showArrows = showArrows,
                onShowArrowsChange = onShowArrowsChange,
                onHideBoard = onToggleBoardVisible,
                highlight = highlight,
                arrows = arrows,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (!isBoardVisible) {
                OutlinedButton(
                    onClick = onToggleBoardVisible,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(text = stringResource(R.string.board_show))
                }
            }

            content()
        }
    }
}
