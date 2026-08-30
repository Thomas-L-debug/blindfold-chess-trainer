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
import com.blindfoldchess.trainer.feature.board.AppShell
import com.blindfoldchess.trainer.feature.board.BoardArrow
import com.blindfoldchess.trainer.feature.board.SquareHighlight
import com.blindfoldchess.trainer.feature.drills.PiecePathDrillScreen
import com.blindfoldchess.trainer.feature.drills.SquareColorDrillScreen
import com.blindfoldchess.trainer.feature.home.HomeScreen
import com.blindfoldchess.trainer.ui.theme.BlindfoldChessTheme

private enum class AppScreen { Home, SquareColor, PiecePath }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlindfoldChessTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var screen by rememberSaveable { mutableStateOf(AppScreen.Home) }
                    var isBoardVisible by rememberSaveable { mutableStateOf(true) }
                    var showNotation by rememberSaveable { mutableStateOf(true) }
                    var showArrows by rememberSaveable { mutableStateOf(true) }
                    var squareHighlight by remember { mutableStateOf<SquareHighlight?>(null) }
                    var moveArrows by remember { mutableStateOf<List<BoardArrow>>(emptyList()) }

                    AppShell(
                        isBoardVisible = isBoardVisible,
                        onToggleBoardVisible = { isBoardVisible = !isBoardVisible },
                        showNotation = showNotation,
                        onShowNotationChange = { showNotation = it },
                        showArrows = showArrows,
                        onShowArrowsChange = { showArrows = it },
                        highlight = squareHighlight,
                        arrows = moveArrows,
                    ) {
                        when (screen) {
                            AppScreen.Home -> HomeScreen(
                                onStartSquareColorDrill = {
                                    moveArrows = emptyList()
                                    screen = AppScreen.SquareColor
                                },
                                onStartPiecePathDrill = { screen = AppScreen.PiecePath },
                            )
                            AppScreen.SquareColor -> SquareColorDrillScreen(
                                onBack = {
                                    squareHighlight = null
                                    moveArrows = emptyList()
                                    screen = AppScreen.Home
                                },
                                onSquareHighlight = { squareHighlight = it },
                            )
                            AppScreen.PiecePath -> PiecePathDrillScreen(
                                onBack = {
                                    squareHighlight = null
                                    moveArrows = emptyList()
                                    screen = AppScreen.Home
                                },
                                onSquareHighlight = { squareHighlight = it },
                                onMoveArrows = { moveArrows = it },
                            )
                        }
                    }
                }
            }
        }
    }
}