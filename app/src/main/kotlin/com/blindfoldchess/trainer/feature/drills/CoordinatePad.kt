package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
fun CoordinatePad(
    pendingFile: Char?,
    enabled: Boolean,
    onFile: (Char) -> Unit,
    onRank: (Int) -> Unit,
    requireFileBeforeRank: Boolean = true,
    highlightedFiles: Set<Char> = emptySet(),
    highlightedRanks: Set<Int> = emptySet(),
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilePad(
            pendingFile = pendingFile,
            enabled = enabled,
            onFile = onFile,
            highlightedFiles = highlightedFiles,
            modifier = Modifier.weight(1f),
        )
        RankPad(
            enabled = enabled && (!requireFileBeforeRank || pendingFile != null),
            onRank = onRank,
            highlightedRanks = highlightedRanks,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun FilePad(
    pendingFile: Char?,
    enabled: Boolean,
    onFile: (Char) -> Unit,
    modifier: Modifier = Modifier,
    highlightedFiles: Set<Char> = emptySet(),
    uppercaseLabels: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FILE_ROWS.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { file ->
                    CoordinateButton(
                        label = if (uppercaseLabels) file.uppercaseChar().toString() else file.toString(),
                        selected = pendingFile == file || file in highlightedFiles,
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onFile(file) },
                    )
                }
            }
        }
    }
}

@Composable
fun RankPad(
    enabled: Boolean,
    onRank: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightedRanks: Set<Int> = emptySet(),
) {
    Column(
        modifier = modifier,
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
                        selected = rank in highlightedRanks,
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onRank(rank) },
                    )
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
