package com.blindfoldchess.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.feature.board.AppShell
import com.blindfoldchess.trainer.feature.board.BoardArrow
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.feature.drills.FamousGamesScreen
import com.blindfoldchess.trainer.feature.drills.FindSquareScreen
import com.blindfoldchess.trainer.feature.drills.FreeBoardScreen
import com.blindfoldchess.trainer.feature.drills.NameSquareScreen
import com.blindfoldchess.trainer.feature.drills.PlayBotScreen
import com.blindfoldchess.trainer.feature.drills.PiecePathDrillScreen
import com.blindfoldchess.trainer.feature.drills.SquareColorDrillScreen
import com.blindfoldchess.trainer.feature.home.AboutScreen
import com.blindfoldchess.trainer.feature.home.HomeScreen
import com.blindfoldchess.trainer.ui.theme.BlindfoldChessTheme

private enum class AppScreen { Home, About, FindSquare, NameSquare, SquareColor, PiecePath, FamousGames, FreeBoard, PlayBot }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlindfoldChessTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
                    var isBoardVisible by rememberSaveable { mutableStateOf(false) }
                    var showNotation by rememberSaveable { mutableStateOf(true) }
                    var showArrows by rememberSaveable { mutableStateOf(true) }
                    var showPieces by rememberSaveable { mutableStateOf(true) }
                    var flipped by rememberSaveable { mutableStateOf(false) }
                    var squareHighlight by remember { mutableStateOf<SquareHighlight?>(null) }
                    var moveArrows by remember { mutableStateOf<List<BoardArrow>>(emptyList()) }
                    var boardPieces by remember { mutableStateOf<List<OccupiedSquare>>(emptyList()) }
                    var selectedSquare by remember { mutableStateOf<Square?>(null) }
                    var onBoardSquareClick by remember { mutableStateOf<((Square) -> Unit)?>(null) }
                    val goHome = {
                        squareHighlight = null
                        moveArrows = emptyList()
                        boardPieces = emptyList()
                        selectedSquare = null
                        onBoardSquareClick = null
                        isBoardVisible = false
                        screen = AppScreen.Home
                    }

                    AppShell(
                        isBoardVisible = isBoardVisible,
                        onToggleBoardVisible = { isBoardVisible = !isBoardVisible },
                        showNotation = showNotation,
                        onShowNotationChange = { showNotation = it },
                        showArrows = showArrows,
                        onShowArrowsChange = { showArrows = it },
                        showPieces = showPieces,
                        onShowPiecesChange = { showPieces = it },
                        flipped = flipped,
                        onFlipBoard = { flipped = !flipped },
                        highlight = squareHighlight,
                        arrows = moveArrows,
                        pieces = boardPieces,
                        selectedSquare = selectedSquare,
                        onSquareClick = onBoardSquareClick,
                    ) {
                        when (screen) {
                            AppScreen.Home -> HomeScreen(
                                onStartFindSquareDrill = {
                                    moveArrows = emptyList()
                                    boardPieces = emptyList()
                                    selectedSquare = null
                                    isBoardVisible = true
                                    screen = AppScreen.FindSquare
                                },
                                onStartNameSquareDrill = {
                                    moveArrows = emptyList()
                                    boardPieces = emptyList()
                                    selectedSquare = null
                                    isBoardVisible = true
                                    screen = AppScreen.NameSquare
                                },
                                onStartSquareColorDrill = {
                                    moveArrows = emptyList()
                                    screen = AppScreen.SquareColor
                                },
                                onStartPiecePathDrill = { screen = AppScreen.PiecePath },
                                onStartFamousGamesDrill = {
                                    moveArrows = emptyList()
                                    screen = AppScreen.FamousGames
                                },
                                onStartFreeBoardDrill = {
                                    moveArrows = emptyList()
                                    screen = AppScreen.FreeBoard
                                },
                                onStartPlayBotDrill = {
                                    moveArrows = emptyList()
                                    screen = AppScreen.PlayBot
                                },
                                onOpenAbout = { screen = AppScreen.About },
                            )
                            AppScreen.About -> AboutScreen(onBack = goHome)
                            AppScreen.FindSquare -> FindSquareScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                                onSquareClickChange = { onBoardSquareClick = it },
                            )
                            AppScreen.NameSquare -> NameSquareScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                            )
                            AppScreen.SquareColor -> SquareColorDrillScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                            )
                            AppScreen.PiecePath -> PiecePathDrillScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                                onMoveArrows = { moveArrows = it },
                            )
                            AppScreen.FamousGames -> FamousGamesScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                                onMoveArrows = { moveArrows = it },
                                onPiecesChange = { boardPieces = it },
                                onSelectedSquareChange = { selectedSquare = it },
                                onSquareClickChange = { onBoardSquareClick = it },
                                onShowBoard = { isBoardVisible = true },
                            )
                            AppScreen.PlayBot -> PlayBotScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                                onMoveArrows = { moveArrows = it },
                                onPiecesChange = { boardPieces = it },
                                onSelectedSquareChange = { selectedSquare = it },
                                onSquareClickChange = { onBoardSquareClick = it },
                            )
                            AppScreen.FreeBoard -> FreeBoardScreen(
                                onBack = goHome,
                                onSquareHighlight = { squareHighlight = it },
                                onMoveArrows = { moveArrows = it },
                                onPiecesChange = { boardPieces = it },
                                onSelectedSquareChange = { selectedSquare = it },
                                onSquareClickChange = { onBoardSquareClick = it },
                            )
                        }
                    }
                }
            }
        }
    }
}