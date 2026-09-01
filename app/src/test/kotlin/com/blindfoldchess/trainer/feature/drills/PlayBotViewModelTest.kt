package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.engine.ChessEngine
import com.blindfoldchess.trainer.engine.parseBestMove
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
        val viewModel = PlayBotViewModel(1500, playerIsWhite = true, engine)
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
        val viewModel = PlayBotViewModel(1500, playerIsWhite = false, engine)

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
        val viewModel = PlayBotViewModel(1500, playerIsWhite = true, engine)
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
        val viewModel = PlayBotViewModel(1500, playerIsWhite = false, engine)
        assertTrue(viewModel.uiState.value.botFailed)
        assertTrue(viewModel.uiState.value.moves.isEmpty())
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

private class ScriptedEngine(
    private val replies: ArrayDeque<String>,
) : ChessEngine {
    override suspend fun bestMove(fen: String, elo: Int): String? = replies.poll()
    override fun close() {}
}

private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
