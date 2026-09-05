package com.blindfoldchess.trainer.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.ui.ActionCard

@Composable
fun HomeScreen(
    onStartFindSquareDrill: () -> Unit,
    onStartNameSquareDrill: () -> Unit,
    onStartSquareColorDrill: () -> Unit,
    onStartPiecePathDrill: () -> Unit,
    onStartFamousGamesDrill: () -> Unit,
    onStartFreeBoardDrill: () -> Unit,
    onStartPlayBotDrill: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(16.dp))

        ActionCard(
            title = stringResource(R.string.drill_find_square_title),
            description = stringResource(R.string.drill_find_square_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartFindSquareDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.drill_name_square_title),
            description = stringResource(R.string.drill_name_square_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartNameSquareDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.drill_square_color_title),
            description = stringResource(R.string.drill_square_color_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartSquareColorDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.drill_piece_path_title),
            description = stringResource(R.string.drill_piece_path_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartPiecePathDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.drill_famous_games_title),
            description = stringResource(R.string.drill_famous_games_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartFamousGamesDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.drill_free_board_title),
            description = stringResource(R.string.drill_free_board_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartFreeBoardDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.drill_play_bot_title),
            description = stringResource(R.string.drill_play_bot_description),
            actionLabel = stringResource(R.string.start_drill),
            onAction = onStartPlayBotDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onOpenAbout) {
            Text(
                text = stringResource(R.string.about_entry),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
