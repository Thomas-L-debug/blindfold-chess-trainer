package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.FindSquareDrill
import com.blindfoldchess.trainer.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque

class NameSquareViewModelTest {

    @Test
    fun `starts with a target square`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        assertEquals("e4", viewModel.uiState.value.target?.algebraic)
        assertFalse(viewModel.uiState.value.answered)
        assertNull(viewModel.uiState.value.wasCorrect)
    }

    @Test
    fun `entering the target is correct`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onFile('e')
        viewModel.onRank(4)

        val state = viewModel.uiState.value
        assertTrue(state.answered)
        assertEquals(true, state.wasCorrect)
        assertEquals("e4", state.lastAttempt?.algebraic)
        assertEquals(1, state.correctCount)
        assertEquals(1, state.totalCount)
        assertNull(state.pendingFile)
    }

    @Test
    fun `entering another square is incorrect`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onFile('e')
        viewModel.onRank(5)

        val state = viewModel.uiState.value
        assertTrue(state.answered)
        assertEquals(false, state.wasCorrect)
        assertEquals("e5", state.lastAttempt?.algebraic)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `ignores pad input while the miss is shown`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onFile('e')
        viewModel.onRank(5)
        viewModel.onFile('e')
        viewModel.onRank(4)

        val state = viewModel.uiState.value
        assertEquals(false, state.wasCorrect)
        assertEquals("e5", state.lastAttempt?.algebraic)
        assertEquals("e4", state.target?.algebraic)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `after a miss the same square can be tried again`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onFile('e')
        viewModel.onRank(5)
        viewModel.unlockRetry()
        viewModel.onFile('e')
        viewModel.onRank(4)

        val state = viewModel.uiState.value
        assertEquals("e4", state.target?.algebraic)
        assertEquals(true, state.wasCorrect)
        assertEquals(1, state.correctCount)
        assertEquals(2, state.totalCount)
    }

    @Test
    fun `rank without a file does nothing`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onRank(4)
        val state = viewModel.uiState.value
        assertFalse(state.answered)
        assertNull(state.lastAttempt)
    }

    @Test
    fun `next question loads a new target`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onFile('e')
        viewModel.onRank(4)
        viewModel.loadNextQuestion()

        val state = viewModel.uiState.value
        assertEquals("a1", state.target?.algebraic)
        assertFalse(state.answered)
        assertNull(state.wasCorrect)
        assertNull(state.lastAttempt)
        assertEquals(1, state.correctCount)
    }

    @Test
    fun `spoken target is correct`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.playSpoken(listOf("E4"))

        val state = viewModel.uiState.value
        assertTrue(state.answered)
        assertEquals(true, state.wasCorrect)
        assertEquals("e4", state.lastAttempt?.algebraic)
        assertEquals("E4", state.lastSpoken)
        assertEquals(1, state.correctCount)
    }

    @Test
    fun `spoken S5 is accepted as f5`() {
        val viewModel = viewModelWithSquares("f5", "a1")
        viewModel.playSpoken(listOf("S5"))
        assertEquals(true, viewModel.uiState.value.wasCorrect)
        assertEquals("f5", viewModel.uiState.value.lastAttempt?.algebraic)
    }

    @Test
    fun `spoken french j ai un is accepted as g1`() {
        val viewModel = viewModelWithSquares("g1", "a1")
        viewModel.playSpoken(listOf("j'ai un"))
        assertEquals(true, viewModel.uiState.value.wasCorrect)
        assertEquals("g1", viewModel.uiState.value.lastAttempt?.algebraic)
    }

    @Test
    fun `spoken e four names the square`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.playSpoken(listOf("e four"))
        assertEquals(true, viewModel.uiState.value.wasCorrect)
        assertEquals("e4", viewModel.uiState.value.lastAttempt?.algebraic)
    }

    @Test
    fun `unclear speech is flagged and does not count as an attempt`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.playSpoken(listOf("P5"))

        val state = viewModel.uiState.value
        assertTrue(state.unrecognized)
        assertFalse(state.answered)
        assertNull(state.wasCorrect)
        assertEquals("P5", state.lastSpoken)
        assertEquals(0, state.totalCount)
        assertEquals("e4", state.target?.algebraic)
    }

    @Test
    fun `spoken input is ignored while the miss is shown`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.playSpoken(listOf("e5"))
        viewModel.playSpoken(listOf("e4"))

        val state = viewModel.uiState.value
        assertEquals(false, state.wasCorrect)
        assertEquals("e5", state.lastAttempt?.algebraic)
        assertEquals(1, state.totalCount)
    }

    private fun viewModelWithSquares(vararg squares: String): NameSquareViewModel {
        val queue = ArrayDeque(squares.map(::sq))
        return NameSquareViewModel(FindSquareDrill { queue.removeFirst() })
    }

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
}
