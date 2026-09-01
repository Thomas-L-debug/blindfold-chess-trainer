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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlinx.coroutines.delay

private const val LEGAL_HIGHLIGHT_MS = 500L
private const val ILLEGAL_HIGHLIGHT_MS = 800L

@Composable
fun FindSquareScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    onSquareClickChange: (((Square) -> Unit)?) -> Unit = {},
    viewModel: FindSquareViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tapHandler = remember<(Square) -> Unit>(viewModel) {
        { square -> viewModel.onSquareTap(square) }
    }

    LaunchedEffect(uiState.flashToken, uiState.wasCorrect, uiState.lastAttempt, uiState.target) {
        val attempt = uiState.lastAttempt
        val correct = uiState.wasCorrect
        if (uiState.flashToken == 0 || attempt == null || correct == null) {
            onSquareHighlight(null)
            return@LaunchedEffect
        }
        val highlighted = if (correct) uiState.target ?: attempt else attempt
        onSquareHighlight(SquareHighlight(highlighted, correct = correct))
        if (correct) {
            delay(LEGAL_HIGHLIGHT_MS)
            onSquareHighlight(null)
            viewModel.loadNextQuestion()
        } else {
            delay(ILLEGAL_HIGHLIGHT_MS)
            onSquareHighlight(null)
            viewModel.unlockRetry()
        }
    }

    LaunchedEffect(uiState.answered, tapHandler) {
        onSquareClickChange(if (uiState.answered) null else tapHandler)
    }

    DisposableEffect(Unit) {
        onDispose {
            onSquareClickChange(null)
            onSquareHighlight(null)
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
            title = stringResource(R.string.drill_find_square_title),
            description = stringResource(R.string.drill_find_square_description),
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = uiState.target?.algebraic?.uppercase().orEmpty().ifEmpty { " " },
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.find_square_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))
        val feedbackColor = when (uiState.wasCorrect) {
            true -> Correct
            false -> Incorrect
            null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
        }
        Text(
            text = when (uiState.wasCorrect) {
                true -> stringResource(R.string.feedback_correct)
                false -> stringResource(R.string.find_square_incorrect)
                null -> " "
            },
            style = MaterialTheme.typography.titleMedium,
            color = feedbackColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(32.dp))

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
