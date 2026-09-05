package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlinx.coroutines.delay

private const val LEGAL_HIGHLIGHT_MS = 500L
private const val ILLEGAL_HIGHLIGHT_MS = 800L

@Composable
fun NameSquareScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    viewModel: NameSquareViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val padEnabled = uiState.target != null && !uiState.answered
    val (speechLanguage, setSpeechLanguage) = rememberVoiceSpeechLanguage()
    val voice = rememberVoiceInput(
        enabled = padEnabled,
        languageTag = speechLanguage.tag,
        onUtterances = viewModel::playSpoken,
    )

    LaunchedEffect(uiState.target, uiState.wasCorrect, uiState.flashToken) {
        val target = uiState.target
        if (target == null) {
            onSquareHighlight(null)
            return@LaunchedEffect
        }
        when (uiState.wasCorrect) {
            true -> {
                onSquareHighlight(SquareHighlight(target, correct = true))
                delay(LEGAL_HIGHLIGHT_MS)
                viewModel.loadNextQuestion()
            }
            false -> {
                onSquareHighlight(SquareHighlight(target, correct = false))
                delay(ILLEGAL_HIGHLIGHT_MS)
                viewModel.unlockRetry()
            }
            null -> onSquareHighlight(SquareHighlight(target, correct = true))
        }
    }

    DisposableEffect(Unit) {
        onDispose { onSquareHighlight(null) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = stringResource(R.string.drill_name_square_title),
            description = stringResource(R.string.drill_name_square_description),
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        val draft = when {
            uiState.lastAttempt != null -> uiState.lastAttempt?.algebraic.orEmpty().uppercase()
            uiState.pendingFile != null -> uiState.pendingFile?.uppercaseChar()?.toString().orEmpty()
            else -> " "
        }
        Text(
            text = draft.ifEmpty { " " },
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildString {
                append(stringResource(R.string.name_square_hint))
                when {
                    voice.listening -> {
                        append(" · ")
                        append(stringResource(R.string.voice_listening))
                    }
                    !uiState.lastSpoken.isNullOrBlank() -> {
                        append(" - ")
                        append(uiState.lastSpoken)
                    }
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (uiState.unrecognized) {
                Incorrect
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))
        val feedback = when {
            uiState.wasCorrect == true -> stringResource(R.string.feedback_correct)
            uiState.wasCorrect == false -> stringResource(R.string.find_square_incorrect)
            uiState.unrecognized -> stringResource(R.string.name_square_unclear)
            voice.error != null -> voice.error
            else -> " "
        }
        Text(
            text = feedback,
            style = MaterialTheme.typography.titleMedium,
            color = when {
                uiState.wasCorrect == true -> Correct
                uiState.wasCorrect == false || uiState.unrecognized -> Incorrect
                voice.error != null -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))
        VoiceSpeakRow(
            enabled = padEnabled,
            voice = voice,
            language = speechLanguage,
            onLanguage = setSpeechLanguage,
        )
        Spacer(modifier = Modifier.height(8.dp))
        CoordinatePad(
            pendingFile = uiState.pendingFile,
            enabled = padEnabled,
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
