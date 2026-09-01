package com.blindfoldchess.trainer.feature.drills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.blindfoldchess.trainer.core.chess.ChessSession
import com.blindfoldchess.trainer.core.chess.PlayResult
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.engine.ChessEngine
import com.blindfoldchess.trainer.engine.StockfishChessEngine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayBotViewModel(
    private val elo: Int,
    private val playerIsWhite: Boolean,
    private val engine: ChessEngine,
    session: ChessSession = ChessSession(),
) : FreeBoardViewModel(session) {

    init {
        _uiState.update {
            it.copy(playerIsWhite = playerIsWhite, botElo = elo)
        }
        maybeRequestBot()
    }

    override fun onLegalMovePlayed() {
        maybeRequestBot()
    }

    override fun onSessionReset() {
        maybeRequestBot()
    }

    override fun undo() {
        if (_uiState.value.botThinking) return
        if (!session.undo()) return
        val after = session.snapshot()
        if (after.atLatest && after.moves.isNotEmpty() && after.isWhiteToMove != playerIsWhite) {
            session.undo()
        }
        publish(session.snapshot())
    }

    override fun onCleared() {
        engine.close()
        super.onCleared()
    }

    private fun maybeRequestBot() {
        val state = _uiState.value
        if (state.gameOver || state.reviewing || state.isPlayerTurn || state.botThinking) return
        viewModelScope.launch {
            _uiState.update { it.copy(botThinking = true, botFailed = false) }
            val uci = runCatching { engine.bestMove(session.fen(), elo) }.getOrNull()
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
    private val elo: Int,
    private val playerIsWhite: Boolean,
    private val engineProvider: () -> ChessEngine = { StockfishChessEngine() },
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayBotViewModel(elo, playerIsWhite, engineProvider()) as T
    }
}
