package com.blindfoldchess.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.blindfoldchess.trainer.feature.board.AppShell
import com.blindfoldchess.trainer.feature.drills.SquareColorDrillScreen
import com.blindfoldchess.trainer.feature.home.HomeScreen
import com.blindfoldchess.trainer.ui.theme.BlindfoldChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlindfoldChessTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showDrill by rememberSaveable { mutableStateOf(false) }
                    var isBoardVisible by rememberSaveable { mutableStateOf(true) }
                    var showNotation by rememberSaveable { mutableStateOf(true) }

                    AppShell(
                        isBoardVisible = isBoardVisible,
                        onToggleBoardVisible = { isBoardVisible = !isBoardVisible },
                        showNotation = showNotation,
                        onShowNotationChange = { showNotation = it },
                    ) {
                        if (showDrill) {
                            SquareColorDrillScreen(onBack = { showDrill = false })
                        } else {
                            HomeScreen(onStartSquareColorDrill = { showDrill = true })
                        }
                    }
                }
            }
        }
    }
}