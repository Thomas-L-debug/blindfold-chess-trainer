package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.blindfoldchess.trainer.core.chess.SquareColor
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlinx.coroutines.delay

private const val SQUARE_HIGHLIGHT_MS = 500L

@Composable
fun SquareColorDrillScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    viewModel: SquareColorDrillViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.answered, uiState.question, uiState.wasCorrect) {
        val question = uiState.question
        val wasCorrect = uiState.wasCorrect
        if (!uiState.answered || question == null || wasCorrect == null) {
            onSquareHighlight(null)
            return@LaunchedEffect
        }
        onSquareHighlight(SquareHighlight(question.square, correct = wasCorrect))
        if (!wasCorrect) playDrillErrorSound()
        delay(SQUARE_HIGHLIGHT_MS)
        onSquareHighlight(null)
        viewModel.loadNextQuestion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = stringResource(R.string.drill_square_color_title),
            description = stringResource(R.string.drill_square_color_description),
            onHome = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = uiState.question?.square?.algebraic.orEmpty(),
            fontSize = 72.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = { viewModel.onAnswer(SquareColor.LIGHT) },
                enabled = !uiState.answered,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.answer_light))
            }
            OutlinedButton(
                onClick = { viewModel.onAnswer(SquareColor.DARK) },
                enabled = !uiState.answered,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.answer_dark))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        val feedbackColor = when (uiState.wasCorrect) {
            true -> Correct
            false -> Incorrect
            null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
        }
        Text(
            text = when (uiState.wasCorrect) {
                true -> stringResource(R.string.feedback_correct)
                false -> stringResource(R.string.feedback_incorrect)
                null -> " "
            },
            style = MaterialTheme.typography.titleMedium,
            color = feedbackColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(
                R.string.score_label,
                uiState.correctCount,
                uiState.totalCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        }

        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        ScreenBottomSpace()
    }
}