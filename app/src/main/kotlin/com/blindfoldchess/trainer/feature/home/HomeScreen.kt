package com.blindfoldchess.trainer.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blindfoldchess.trainer.R

@Composable
fun HomeScreen(
    onStartFindSquareDrill: () -> Unit,
    onStartSquareColorDrill: () -> Unit,
    onStartPiecePathDrill: () -> Unit,
    onStartFamousGamesDrill: () -> Unit,
    onStartFreeBoardDrill: () -> Unit,
    onStartPlayBotDrill: () -> Unit,
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

        DrillCard(
            title = stringResource(R.string.drill_find_square_title),
            description = stringResource(R.string.drill_find_square_description),
            onStart = onStartFindSquareDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrillCard(
            title = stringResource(R.string.drill_square_color_title),
            description = stringResource(R.string.drill_square_color_description),
            onStart = onStartSquareColorDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrillCard(
            title = stringResource(R.string.drill_piece_path_title),
            description = stringResource(R.string.drill_piece_path_description),
            onStart = onStartPiecePathDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrillCard(
            title = stringResource(R.string.drill_famous_games_title),
            description = stringResource(R.string.drill_famous_games_description),
            onStart = onStartFamousGamesDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrillCard(
            title = stringResource(R.string.drill_free_board_title),
            description = stringResource(R.string.drill_free_board_description),
            onStart = onStartFreeBoardDrill,
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrillCard(
            title = stringResource(R.string.drill_play_bot_title),
            description = stringResource(R.string.drill_play_bot_description),
            onStart = onStartPlayBotDrill,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private val DrillFrameHeight = 96.dp
private val DrillFrameShape = RoundedCornerShape(12.dp)

@Composable
private fun DrillCard(
    title: String,
    description: String,
    onStart: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(DrillFrameHeight),
        shape = DrillFrameShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(
                onClick = onStart,
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.start_drill),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}
