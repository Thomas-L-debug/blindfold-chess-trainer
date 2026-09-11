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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.core.chess.ChessMoveAnnouncer
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect


@Composable
internal fun FreeBoardPlayBody(
    title: String,
    description: String,
    uiState: FreeBoardUiState,
    extraHint: String? = null,
    botLastMove: String? = null,
    botLastMoveSan: String? = null,
    botLastMovePly: Int? = null,
    viewModel: FreeBoardViewModel,
    onBack: () -> Unit,
    onPlayAgain: (() -> Unit)? = null,
) {
    val padEnabled = uiState.inputEnabled && uiState.disambiguation == null
    val (speechLanguage, setSpeechLanguage) = rememberVoiceSpeechLanguage()
    val voice = rememberVoiceInput(
        enabled = uiState.inputEnabled,
        languageTag = speechLanguage.tag,
        onUtterances = viewModel::playSpoken,
    )
    val tts = rememberChessTts()
    var announcedBotPly by rememberSaveable { mutableIntStateOf(-1) }
    var announcedBotLang by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(voice.listening) {
        if (voice.listening) tts.stop()
    }
    DrillErrorSoundEffect(
        play = uiState.lastAttemptCorrect == false,
        token = uiState.flashToken,
    )
    LaunchedEffect(botLastMovePly, botLastMoveSan, speechLanguage) {
        val ply = botLastMovePly
        val san = botLastMoveSan.orEmpty()
        if (ply == null || san.isBlank()) return@LaunchedEffect
        val phrase = ChessMoveAnnouncer.spoken(san, speechLanguage.announcer)
        if (phrase.isBlank()) return@LaunchedEffect
        val langChanged = announcedBotLang.isNotEmpty() && announcedBotLang != speechLanguage.tag
        when {
            ply < announcedBotPly -> announcedBotPly = ply
            ply == announcedBotPly && !langChanged -> return@LaunchedEffect
            else -> {
                announcedBotPly = ply
                announcedBotLang = speechLanguage.tag
                tts.speak(phrase, speechLanguage.tag)
            }
        }
    }
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

        val status = statusText(uiState)
        val statusColor = when {
            uiState.isCheckmate || uiState.isStalemate -> Correct
            uiState.inCheck -> Incorrect
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        val moveDraft = uiState.moveDraft.uppercase()
        val illegal = uiState.lastAttemptCorrect == false
        val draftColor = if (illegal) Incorrect else MaterialTheme.colorScheme.primary
        val spoken = uiState.lastSpoken
        val statusLine = buildAnnotatedString {
            append(status)
            if (uiState.botThinking) {
                append(" · ")
                append(stringResource(R.string.play_bot_thinking))
            } else if (voice.listening) {
                append(" · ")
                append(stringResource(R.string.voice_listening))
            }
            when {
                moveDraft.isNotEmpty() -> {
                    append(" - ")
                    withStyle(SpanStyle(color = draftColor)) {
                        if (illegal) {
                            append("$moveDraft - ${stringResource(R.string.free_board_illegal)}")
                        } else {
                            append(moveDraft)
                        }
                    }
                }
                !spoken.isNullOrBlank() -> {
                    append(" - ")
                    withStyle(SpanStyle(color = draftColor)) {
                        if (illegal) {
                            append("$spoken - ${stringResource(R.string.free_board_illegal)}")
                        } else {
                            append(spoken)
                        }
                    }
                }
                illegal -> {
                    append(" - ")
                    withStyle(SpanStyle(color = draftColor)) {
                        append(stringResource(R.string.free_board_illegal))
                    }
                }
            }
        }
        if (onPlayAgain != null && uiState.gameOver) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = statusLine,
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onPlayAgain) {
                    Text(stringResource(R.string.play_bot_play_again))
                }
            }
        } else {
            Text(
                text = statusLine,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val contextHint = extraHint ?: voice.error ?: when {
            uiState.botFailed -> stringResource(R.string.play_bot_failed)
            uiState.disambiguation?.askFile == true -> stringResource(R.string.free_board_choose_file)
            uiState.disambiguation != null -> stringResource(R.string.free_board_choose_rank)
            uiState.reviewing -> stringResource(R.string.free_board_review_hint)
            else -> null
        }
        if (contextHint != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = contextHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (botLastMove != null) {
            Text(
                text = if (botLastMove.isEmpty()) " " else stringResource(R.string.play_bot_last_move),
                style = MaterialTheme.typography.bodyMedium,
                color = if (botLastMove.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = botLastMove.ifEmpty { " " },
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                color = if (botLastMove.isEmpty()) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                } else {
                    MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        InputMethodControls(
            voiceEnabled = uiState.inputEnabled,
            voice = voice,
            language = speechLanguage,
            onLanguage = setSpeechLanguage,
        ) {
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
                        uppercaseLabels = false,
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
            ReplayNavigation(
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
                Text(
                    text = stringResource(R.string.free_board_undo),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
            OutlinedButton(
                onClick = viewModel::reset,
                enabled = !uiState.botThinking,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.free_board_reset),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        ScreenBottomSpace()
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
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(10.dp),
    ) {
        content()
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
        uiState.isCheckmate -> stringResource(R.string.free_board_checkmate)
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
