package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.engine.BOT_ELO_LEVELS
import com.blindfoldchess.trainer.feature.board.BoardArrow
import com.blindfoldchess.trainer.feature.board.SquareHighlight

@Composable
fun PlayBotScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    onMoveArrows: (List<BoardArrow>) -> Unit = {},
    onPiecesChange: (List<OccupiedSquare>) -> Unit = {},
    onSelectedSquareChange: (Square?) -> Unit = {},
    onSquareClickChange: (((Square) -> Unit)?) -> Unit = {},
) {
    var started by rememberSaveable { mutableStateOf(false) }
    var elo by rememberSaveable { mutableIntStateOf(1500) }
    var playerIsWhite by rememberSaveable { mutableStateOf(true) }

    if (!started) {
        PlayBotSetup(
            elo = elo,
            onEloChange = { elo = it },
            playerIsWhite = playerIsWhite,
            onPlayerIsWhiteChange = { playerIsWhite = it },
            onStart = { started = true },
            onBack = onBack,
        )
        return
    }

    val viewModel: PlayBotViewModel = viewModel(
        key = "play-bot-$elo-$playerIsWhite",
        factory = PlayBotViewModelFactory(elo, playerIsWhite),
    )
    val uiState by viewModel.uiState.collectAsState()

    BoardPlayEffects(
        uiState = uiState,
        viewModel = viewModel,
        onSquareHighlight = onSquareHighlight,
        onMoveArrows = onMoveArrows,
        onPiecesChange = onPiecesChange,
        onSelectedSquareChange = onSelectedSquareChange,
        onSquareClickChange = onSquareClickChange,
    )

    val extraHint = when {
        uiState.botThinking -> stringResource(R.string.play_bot_thinking)
        uiState.botFailed -> stringResource(R.string.play_bot_failed)
        else -> null
    }

    FreeBoardPlayBody(
        title = stringResource(R.string.drill_play_bot_title),
        description = stringResource(R.string.play_bot_playing_as, elo, stringResource(
            if (playerIsWhite) R.string.famous_games_white else R.string.famous_games_black,
        )),
        uiState = uiState,
        extraHint = extraHint,
        viewModel = viewModel,
        onBack = onBack,
    )
}

@Composable
private fun PlayBotSetup(
    elo: Int,
    onEloChange: (Int) -> Unit,
    playerIsWhite: Boolean,
    onPlayerIsWhiteChange: (Boolean) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = stringResource(R.string.drill_play_bot_title),
            description = stringResource(R.string.drill_play_bot_description),
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.play_bot_choose_elo),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        BOT_ELO_LEVELS.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                row.forEach { level ->
                    FilterChip(
                        selected = elo == level,
                        onClick = { onEloChange(level) },
                        label = { Text(level.toString()) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.play_bot_choose_color),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val whiteModifier = Modifier.weight(1f)
            if (playerIsWhite) {
                Button(onClick = { onPlayerIsWhiteChange(true) }, modifier = whiteModifier) {
                    Text(stringResource(R.string.famous_games_white), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                OutlinedButton(onClick = { onPlayerIsWhiteChange(true) }, modifier = whiteModifier) {
                    Text(stringResource(R.string.famous_games_white), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            val blackModifier = Modifier.weight(1f)
            if (!playerIsWhite) {
                Button(onClick = { onPlayerIsWhiteChange(false) }, modifier = blackModifier) {
                    Text(stringResource(R.string.famous_games_black), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                OutlinedButton(onClick = { onPlayerIsWhiteChange(false) }, modifier = blackModifier) {
                    Text(stringResource(R.string.famous_games_black), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.play_bot_start))
        }

        Spacer(modifier = Modifier.height(16.dp))
        DrillBackButton(onClick = onBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
