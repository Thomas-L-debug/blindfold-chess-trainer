package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.engine.ChessEngine
import com.blindfoldchess.trainer.engine.parseBestMove
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.ArrayDeque

@OptIn(ExperimentalCoroutinesApi::class)
class PlayBotViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setMain() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    @Test
    fun `bot replies after a legal player move`() {
        val engine = ScriptedEngine(ArrayDeque(listOf("e7e5")))
        val viewModel = playBot(engine)
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("e4"))

        val state = viewModel.uiState.value
        assertEquals(2, state.moves.size)
        assertEquals("e4", state.moves[0].san)
        assertEquals("e7", state.moves[1].from.algebraic)
        assertEquals("e5", state.moves[1].to.algebraic)
        assertTrue(state.isWhiteToMove)
        assertFalse(state.botThinking)
        assertTrue(state.inputEnabled)
    }

    @Test
    fun `bot moves first when the player is black`() {
        val engine = ScriptedEngine(ArrayDeque(listOf("e2e4")))
        val viewModel = playBot(engine, playerIsWhite = false)

        val state = viewModel.uiState.value
        assertEquals(1, state.moves.size)
        assertEquals("e2", state.moves[0].from.algebraic)
        assertEquals("e4", state.moves[0].to.algebraic)
        assertFalse(state.isWhiteToMove)
        assertTrue(state.inputEnabled)
    }

    @Test
    fun `undo takes back the bot reply and the player move`() {
        val engine = ScriptedEngine(ArrayDeque(listOf("e7e5")))
        val viewModel = playBot(engine)
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("e4"))
        assertEquals(2, viewModel.uiState.value.moves.size)

        viewModel.undo()

        val state = viewModel.uiState.value
        assertTrue(state.moves.isEmpty())
        assertTrue(state.isWhiteToMove)
        assertEquals(32, state.pieces.size)
    }

    @Test
    fun `missing bot move marks a failure`() {
        val engine = ScriptedEngine(ArrayDeque())
        val viewModel = playBot(engine, playerIsWhite = false)
        assertTrue(viewModel.uiState.value.botFailed)
        assertTrue(viewModel.uiState.value.moves.isEmpty())
    }

    @Test
    fun `leaving a started game asks to resume`() {
        val viewModel = playBot(ScriptedEngine(ArrayDeque()))
        assertEquals(PlayBotPhase.Playing, viewModel.phase.value)

        viewModel.onLeaveScreen()

        assertEquals(PlayBotPhase.ResumePrompt, viewModel.phase.value)
        assertEquals(1500, viewModel.uiState.value.botElo)
        assertEquals(true, viewModel.uiState.value.playerIsWhite)
    }

    @Test
    fun `continue restores the same game`() {
        val engine = ScriptedEngine(ArrayDeque(listOf("e7e5")))
        val viewModel = playBot(engine)
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("e4"))
        viewModel.onLeaveScreen()

        viewModel.continueGame()

        assertEquals(PlayBotPhase.Playing, viewModel.phase.value)
        assertEquals(2, viewModel.uiState.value.moves.size)
        assertEquals("e5", viewModel.uiState.value.moves[1].san)
    }

    @Test
    fun `discard returns to setup and a new game can use the same engine`() {
        val engine = ScriptedEngine(ArrayDeque(listOf("e7e5", "e7e5")))
        val viewModel = playBot(engine)
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("e4"))
        viewModel.onLeaveScreen()

        viewModel.discardGame()

        assertEquals(PlayBotPhase.Setup, viewModel.phase.value)
        assertTrue(viewModel.uiState.value.moves.isEmpty())
        assertNull(viewModel.uiState.value.botElo)
        assertNull(viewModel.uiState.value.playerIsWhite)

        viewModel.startGame(1900, playerIsWhite = true)
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("e4"))

        assertEquals(PlayBotPhase.Playing, viewModel.phase.value)
        assertEquals(1900, viewModel.uiState.value.botElo)
        assertEquals(2, viewModel.uiState.value.moves.size)
        assertEquals("e5", viewModel.uiState.value.moves[1].san)
    }

    @Test
    fun `discard ignores a bot move from the previous game`() {
        val engine = GateEngine()
        val viewModel = PlayBotViewModel(engine)
        viewModel.startGame(1500, playerIsWhite = false)
        assertTrue(viewModel.uiState.value.botThinking)

        viewModel.discardGame()
        viewModel.startGame(1500, playerIsWhite = true)
        engine.reply.complete("e2e4")

        val state = viewModel.uiState.value
        assertTrue(state.moves.isEmpty())
        assertTrue(state.isWhiteToMove)
        assertTrue(state.isPlayerTurn)
        assertFalse(state.botThinking)
    }
}

class ParseBestMoveTest {
    @Test
    fun `reads the uci token`() {
        assertEquals("e7e5", parseBestMove("bestmove e7e5 ponder e2e4"))
        assertEquals("e7e8q", parseBestMove("bestmove e7e8q"))
        assertNull(parseBestMove("bestmove (none)"))
        assertNull(parseBestMove("info depth 8"))
    }
}

private fun playBot(
    engine: ChessEngine,
    elo: Int = 1500,
    playerIsWhite: Boolean = true,
): PlayBotViewModel = PlayBotViewModel(engine).also { it.startGame(elo, playerIsWhite) }

private class ScriptedEngine(
    private val replies: ArrayDeque<String>,
) : ChessEngine {
    override suspend fun bestMove(fen: String, elo: Int): String? = replies.poll()
    override fun close() {}
}

private class GateEngine : ChessEngine {
    val reply = CompletableDeferred<String?>()
    override suspend fun bestMove(fen: String, elo: Int): String? = reply.await()
    override fun close() {}
}

private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
