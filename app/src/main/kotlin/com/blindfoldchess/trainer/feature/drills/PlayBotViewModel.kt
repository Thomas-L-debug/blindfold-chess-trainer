package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blindfoldchess.trainer.core.chess.ChessSession
import com.blindfoldchess.trainer.core.chess.PlayResult
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.engine.ChessEngine
import com.blindfoldchess.trainer.engine.StockfishChessEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlayBotPhase { Setup, ResumePrompt, Playing }

class PlayBotViewModel(
    private val engine: ChessEngine,
    session: ChessSession = ChessSession(),
) : FreeBoardViewModel(session) {

    private val _phase = MutableStateFlow(PlayBotPhase.Setup)
    val phase: StateFlow<PlayBotPhase> = _phase.asStateFlow()

    private var gameGeneration = 0
    private var botJob: Job? = null

    fun startGame(elo: Int, playerIsWhite: Boolean) {
        beginGeneration()
        session.reset()
        _phase.value = PlayBotPhase.Playing
        _uiState.update { it.copy(playerIsWhite = playerIsWhite, botElo = elo) }
        publish(session.snapshot())
        maybeRequestBot()
    }

    fun continueGame() {
        if (_phase.value == PlayBotPhase.Setup) return
        _phase.value = PlayBotPhase.Playing
        maybeRequestBot()
    }

    fun discardGame() {
        beginGeneration()
        session.reset()
        _phase.value = PlayBotPhase.Setup
        publish(session.snapshot())
        _uiState.update {
            it.copy(
                playerIsWhite = null,
                botElo = null,
                botThinking = false,
                botFailed = false,
            )
        }
    }

    fun onLeaveScreen() {
        if (_phase.value == PlayBotPhase.Playing) {
            _phase.value = PlayBotPhase.ResumePrompt
        }
    }

    override fun onLegalMovePlayed() {
        maybeRequestBot()
    }

    override fun onSessionReset() {
        maybeRequestBot()
    }

    override fun undo() {
        val playerIsWhite = _uiState.value.playerIsWhite ?: return
        if (_uiState.value.botThinking) return
        if (!session.undo()) return
        val after = session.snapshot()
        if (after.atLatest && after.moves.isNotEmpty() && after.isWhiteToMove != playerIsWhite) {
            session.undo()
        }
        publish(session.snapshot())
    }

    override fun onCleared() {
        beginGeneration()
        engine.close()
        super.onCleared()
    }

    private fun beginGeneration() {
        gameGeneration += 1
        botJob?.cancel()
        botJob = null
    }

    private fun maybeRequestBot() {
        val state = _uiState.value
        val elo = state.botElo ?: return
        if (state.gameOver || state.reviewing || state.isPlayerTurn || state.botThinking) return
        val generation = gameGeneration
        botJob = viewModelScope.launch {
            _uiState.update { it.copy(botThinking = true, botFailed = false) }
            val uci = runCatching { engine.bestMove(session.fen(), elo) }.getOrNull()
            if (generation != gameGeneration) return@launch
            if (uci == null) {
                _uiState.update { it.copy(botThinking = false, botFailed = true) }
                return@launch
            }
            val from = Square.fromAlgebraic(uci.take(2))
            val to = Square.fromAlgebraic(uci.drop(2).take(2))
            when (val result = session.playUci(uci)) {
                is PlayResult.Played -> applyResult(result, attemptedFrom = from, attemptedTo = to)
                else -> _uiState.update { it.copy(botThinking = false, botFailed = true) }
            }
        }
    }
}

class PlayBotViewModelFactory(
    private val engineProvider: () -> ChessEngine = { StockfishChessEngine() },
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayBotViewModel(engineProvider()) as T
    }
}
