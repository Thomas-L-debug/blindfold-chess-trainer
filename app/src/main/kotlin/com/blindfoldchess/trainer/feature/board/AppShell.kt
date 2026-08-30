package com.blindfoldchess.trainer.feature.board

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isBoardVisible) {
            BoardPanel(
                showNotation = showNotation,
                onShowNotationChange = onShowNotationChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            OutlinedButton(
                onClick = onToggleBoardVisible,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(
                        if (isBoardVisible) R.string.board_hide else R.string.board_show,
                    ),
                )
            }

            content()
        }
    }
}