package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlinx.coroutines.delay

private const val ILLEGAL_MESSAGE_MS = 1500L

@Composable
internal fun FreeBoardPlayBody(
    title: String,
    description: String,
    uiState: FreeBoardUiState,
    extraHint: String? = null,
    viewModel: FreeBoardViewModel,
    onBack: () -> Unit,
) {
    val padEnabled = uiState.inputEnabled && uiState.disambiguation == null
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = title,
            description = description,
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = statusText(uiState),
            style = MaterialTheme.typography.titleMedium,
            color = when {
                uiState.isCheckmate || uiState.isStalemate -> Correct
                uiState.inCheck -> Incorrect
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        val contextHint = extraHint ?: when {
            uiState.disambiguation?.askFile == true -> stringResource(R.string.free_board_choose_file)
            uiState.disambiguation != null -> stringResource(R.string.free_board_choose_rank)
            uiState.reviewing -> stringResource(R.string.free_board_review_hint)
            else -> null
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contextHint ?: " ",
            style = MaterialTheme.typography.bodyMedium,
            color = if (contextHint == null) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(4.dp))
        val moveDraft = uiState.moveDraft.uppercase()
        var showIllegal by remember { mutableStateOf(false) }
        LaunchedEffect(uiState.flashToken, uiState.lastAttemptCorrect) {
            if (uiState.lastAttemptCorrect == false) {
                showIllegal = true
                delay(ILLEGAL_MESSAGE_MS)
                showIllegal = false
            } else {
                showIllegal = false
            }
        }
        val illegal = showIllegal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    moveDraft.isNotEmpty() && illegal ->
                        "$moveDraft - ${stringResource(R.string.free_board_illegal)}"
                    illegal -> stringResource(R.string.free_board_illegal)
                    moveDraft.isNotEmpty() -> moveDraft
                    else -> " "
                },
                style = MaterialTheme.typography.titleLarge,
                color = when {
                    illegal -> Incorrect
                    moveDraft.isEmpty() -> MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                    else -> MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        KeyFrame {
            PieceTypeSelector(
                selected = uiState.selectedMan,
                enabled = padEnabled,
                onSelect = viewModel::selectMan,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        KeyFrame {
            CaptureAndCastleRow(
                capturing = uiState.capturing,
                enabled = padEnabled,
                onToggleCapture = viewModel::toggleCapture,
                onCastle = viewModel::castle,
            )
        }

        val disambiguation = uiState.disambiguation
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KeyFrame(modifier = Modifier.weight(1f)) {
                FilePad(
                    pendingFile = uiState.pendingFile,
                    enabled = if (disambiguation != null) {
                        disambiguation.askFile
                    } else {
                        uiState.inputEnabled
                    },
                    onFile = viewModel::onFile,
                    highlightedFiles = when {
                        disambiguation?.askFile == true ->
                            disambiguation.options.map { it[0] }.toSet()
                        else -> setOfNotNull(uiState.originFile)
                    },
                    uppercaseLabels = true,
                )
            }
            KeyFrame(modifier = Modifier.weight(1f)) {
                RankPad(
                    enabled = if (disambiguation != null) {
                        !disambiguation.askFile
                    } else {
                        uiState.inputEnabled
                    },
                    onRank = viewModel::onRank,
                    highlightedRanks = when {
                        disambiguation != null && !disambiguation.askFile ->
                            disambiguation.options.mapNotNull { it.toIntOrNull() }.toSet()
                        else -> setOfNotNull(uiState.originRank)
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = uiState.playedMoves.ifEmpty { " " },
            style = MaterialTheme.typography.bodySmall,
            color = if (uiState.playedMoves.isEmpty()) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        KeyFrame {
            LineNavigation(
                canGoToStart = uiState.canGoToStart && !uiState.botThinking,
                canStepBack = uiState.canStepBack && !uiState.botThinking,
                canStepForward = uiState.canStepForward && !uiState.botThinking,
                canGoToLatest = uiState.canGoToLatest && !uiState.botThinking,
                onGoToStart = viewModel::goToStart,
                onStepBack = viewModel::stepBack,
                onStepForward = viewModel::stepForward,
                onGoToLatest = viewModel::goToLatest,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = viewModel::undo,
                enabled = !uiState.botThinking && uiState.atLatest && uiState.moves.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.free_board_undo))
            }
            OutlinedButton(
                onClick = viewModel::reset,
                enabled = !uiState.botThinking,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.free_board_reset))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
internal fun KeyFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(10.dp),
    ) {
        content()
    }
}

@Composable
private fun LineNavigation(
    canGoToStart: Boolean,
    canStepBack: Boolean,
    canStepForward: Boolean,
    canGoToLatest: Boolean,
    onGoToStart: () -> Unit,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onGoToLatest: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NavButton(
            label = stringResource(R.string.famous_games_nav_start),
            enabled = canGoToStart,
            onClick = onGoToStart,
            modifier = Modifier.weight(1f),
        )
        NavButton(
            label = stringResource(R.string.famous_games_nav_back),
            enabled = canStepBack,
            onClick = onStepBack,
            modifier = Modifier.weight(1f),
            largeGlyph = true,
        )
        NavButton(
            label = stringResource(R.string.famous_games_nav_forward),
            enabled = canStepForward,
            onClick = onStepForward,
            modifier = Modifier.weight(1f),
            largeGlyph = true,
        )
        NavButton(
            label = stringResource(R.string.famous_games_nav_latest),
            enabled = canGoToLatest,
            onClick = onGoToLatest,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NavButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    largeGlyph: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(ButtonDefaults.MinHeight),
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val glyphLift = with(LocalDensity.current) { if (largeGlyph) -6.dp.toPx() else 0f }
            Text(
                text = label,
                modifier = Modifier.graphicsLayer { translationY = glyphLift },
                fontSize = if (largeGlyph) 28.sp else MaterialTheme.typography.labelLarge.fontSize,
                lineHeight = if (largeGlyph) 28.sp else MaterialTheme.typography.labelLarge.lineHeight,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
            )
        }
    }
}

@Composable
private fun PadToggleButton(
    selected: Boolean,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            CenteredPadLabel(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            CenteredPadLabel(label)
        }
    }
}

@Composable
private fun CenteredPadLabel(label: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun PieceTypeSelector(
    selected: ChessMan?,
    enabled: Boolean,
    onSelect: (ChessMan) -> Unit,
) {
    val pieces = listOf(
        ChessMan.KNIGHT,
        ChessMan.BISHOP,
        ChessMan.ROOK,
        ChessMan.QUEEN,
        ChessMan.KING,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        pieces.forEach { man ->
            PadToggleButton(
                selected = man == selected,
                enabled = enabled,
                label = pieceLetter(man),
                onClick = { onSelect(man) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CaptureAndCastleRow(
    capturing: Boolean,
    enabled: Boolean,
    onToggleCapture: () -> Unit,
    onCastle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val captureModifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        if (capturing) {
            Button(
                onClick = onToggleCapture,
                enabled = enabled,
                modifier = captureModifier,
            ) {
                Text(
                    text = stringResource(R.string.free_board_capture),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            OutlinedButton(
                onClick = onToggleCapture,
                enabled = enabled,
                modifier = captureModifier,
            ) {
                Text(
                    text = stringResource(R.string.free_board_capture),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        OutlinedButton(
            onClick = { onCastle(true) },
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Text(stringResource(R.string.free_board_castle_short))
        }
        OutlinedButton(
            onClick = { onCastle(false) },
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            Text(stringResource(R.string.free_board_castle_long))
        }
    }
}

@Composable
private fun statusText(uiState: FreeBoardUiState): String {
    val side = stringResource(
        if (uiState.isWhiteToMove) R.string.famous_games_white else R.string.famous_games_black,
    )
    return when {
        uiState.isCheckmate -> stringResource(R.string.free_board_checkmate, side)
        uiState.isStalemate -> stringResource(R.string.free_board_stalemate)
        uiState.inCheck -> stringResource(R.string.free_board_check, side)
        else -> stringResource(R.string.famous_games_side, side)
    }
}

private fun pieceLetter(man: ChessMan): String = when (man) {
    ChessMan.KING -> "K"
    ChessMan.KNIGHT -> "N"
    ChessMan.QUEEN -> "Q"
    ChessMan.BISHOP -> "B"
    ChessMan.ROOK -> "R"
    ChessMan.PAWN -> ""
}
