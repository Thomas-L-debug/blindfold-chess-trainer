package com.blindfoldchess.trainer.feature.drills

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.blindfoldchess.trainer.R
import com.blindfoldchess.trainer.core.chess.FamousGame
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.feature.board.BoardArrow
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlinx.coroutines.delay

private const val LEGAL_HIGHLIGHT_MS = 500L
private const val ILLEGAL_HIGHLIGHT_MS = 1500L

@Composable
fun FamousGamesScreen(
    onBack: () -> Unit,
    onSquareHighlight: (SquareHighlight?) -> Unit = {},
    onMoveArrows: (List<BoardArrow>) -> Unit = {},
    onPiecesChange: (List<OccupiedSquare>) -> Unit = {},
    onSelectedSquareChange: (Square?) -> Unit = {},
    onSquareClickChange: (((Square) -> Unit)?) -> Unit = {},
    viewModel: FamousGamesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val tapHandler = remember<(Square) -> Unit>(viewModel) {
        { square -> viewModel.onSquareTap(square) }
    }

    LaunchedEffect(
        uiState.browsing,
        uiState.flashToken,
        uiState.lastPlayedFrom,
        uiState.lastPlayedTo,
        uiState.lastAttemptCorrect,
        uiState.lastAttemptFrom,
        uiState.lastAttemptSquare,
    ) {
        val square = uiState.lastAttemptSquare
        val correct = uiState.lastAttemptCorrect
        val legalFrom = uiState.lastPlayedFrom
        val legalTo = uiState.lastPlayedTo

        fun showPlayedArrow() {
            if (uiState.browsing || legalFrom == null || legalTo == null) {
                onMoveArrows(emptyList())
            } else {
                onMoveArrows(listOf(BoardArrow(legalFrom, legalTo)))
            }
        }

        if (uiState.flashToken == 0 || square == null || correct == null) {
            onSquareHighlight(null)
            showPlayedArrow()
            return@LaunchedEffect
        }

        onSquareHighlight(SquareHighlight(square, correct = correct))
        if (!correct) {
            val attemptFrom = uiState.lastAttemptFrom ?: square
            onMoveArrows(listOf(BoardArrow(attemptFrom, square)))
        } else {
            showPlayedArrow()
        }
        delay(if (correct) LEGAL_HIGHLIGHT_MS else ILLEGAL_HIGHLIGHT_MS)
        onSquareHighlight(null)
        showPlayedArrow()
    }

    LaunchedEffect(uiState.pieces) {
        onPiecesChange(uiState.pieces)
    }

    LaunchedEffect(uiState.fromSquare, uiState.browsing) {
        onSelectedSquareChange(if (uiState.browsing) null else uiState.fromSquare)
    }

    LaunchedEffect(uiState.boardTappable, tapHandler) {
        onSquareClickChange(if (uiState.boardTappable) tapHandler else null)
    }

    DisposableEffect(Unit) {
        onDispose {
            onPiecesChange(emptyList())
            onSelectedSquareChange(null)
            onSquareClickChange(null)
            onMoveArrows(emptyList())
            onSquareHighlight(null)
        }
    }

    if (uiState.browsing) {
        FamousGamesLibrary(
            games = uiState.games,
            onSelect = viewModel::selectGame,
            onBack = onBack,
        )
    } else {
        FamousGameReplay(
            uiState = uiState,
            onStepBack = viewModel::stepBack,
            onStepForward = viewModel::stepForward,
            onGoToStart = viewModel::goToStart,
            onGoToLatest = viewModel::goToLatest,
            onReplay = viewModel::replay,
            onBackToLibrary = viewModel::backToLibrary,
            onBack = onBack,
        )
    }
}

@Composable
private fun FamousGamesLibrary(
    games: List<FamousGame>,
    onSelect: (String) -> Unit,
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
            title = stringResource(R.string.drill_famous_games_title),
            description = stringResource(R.string.drill_famous_games_description),
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(20.dp))

        games.forEach { game ->
            FamousGameCard(game = game, onPlay = { onSelect(game.id) })
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        DrillBackButton(onClick = onBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FamousGameCard(
    game: FamousGame,
    onPlay: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.famous_games_vs, game.white, game.black),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    R.string.famous_games_meta,
                    game.event,
                    game.year,
                    game.fullMoveCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.famous_games_play))
            }
        }
    }
}

@Composable
private fun FamousGameReplay(
    uiState: FamousGamesUiState,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onGoToStart: () -> Unit,
    onGoToLatest: () -> Unit,
    onReplay: () -> Unit,
    onBackToLibrary: () -> Unit,
    onBack: () -> Unit,
) {
    val game = uiState.game
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrillPageHeader(
            title = game?.title.orEmpty(),
            description = if (game != null) {
                stringResource(R.string.famous_games_vs, game.white, game.black)
            } else {
                " "
            },
            onHome = onBack,
        )

        Spacer(modifier = Modifier.height(16.dp))

        val current = uiState.currentMove
        val completed = uiState.completed && uiState.atFrontier
        Text(
            text = if (completed) {
                " "
            } else {
                stringResource(
                    R.string.famous_games_side,
                    if (current?.isWhite == true) {
                        stringResource(R.string.famous_games_white)
                    } else {
                        stringResource(R.string.famous_games_black)
                    },
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (completed) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (completed) {
                stringResource(R.string.famous_games_complete)
            } else {
                current?.prompt.orEmpty().ifEmpty { " " }
            },
            fontSize = 36.sp,
            fontWeight = FontWeight.Light,
            color = if (completed) Correct else MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (completed) {
                " "
            } else {
                stringResource(
                    if (uiState.reviewing) {
                        R.string.famous_games_review_hint
                    } else {
                        R.string.famous_games_hint
                    },
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (completed) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReplayNavigation(
            canGoToStart = uiState.canGoToStart,
            canStepBack = uiState.canStepBack,
            canStepForward = uiState.canStepForward,
            canGoToLatest = uiState.canGoToLatest,
            onGoToStart = onGoToStart,
            onStepBack = onStepBack,
            onStepForward = onStepForward,
            onGoToLatest = onGoToLatest,
        )

        var showIllegal by remember { mutableStateOf(false) }
        LaunchedEffect(uiState.flashToken, uiState.lastAttemptCorrect) {
            if (uiState.lastAttemptCorrect == false) {
                showIllegal = true
                delay(ILLEGAL_HIGHLIGHT_MS)
                showIllegal = false
            } else {
                showIllegal = false
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (showIllegal) {
                stringResource(R.string.famous_games_try_again)
            } else {
                " "
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (showIllegal) Incorrect else Incorrect.copy(alpha = 0f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

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

        Spacer(modifier = Modifier.height(20.dp))

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

        Button(
            onClick = onReplay,
            enabled = uiState.completed,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.famous_games_replay))
        }
        Spacer(modifier = Modifier.height(8.dp))

        DrillBackButton(
            onClick = onBackToLibrary,
            label = stringResource(R.string.famous_games_library),
        )
        Spacer(modifier = Modifier.height(8.dp))
        DrillBackButton(onClick = onBack)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReplayNavigation(
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


