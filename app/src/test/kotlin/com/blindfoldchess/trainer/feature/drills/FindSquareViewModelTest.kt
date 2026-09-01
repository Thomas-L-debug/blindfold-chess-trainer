package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.FindSquareDrill
import com.blindfoldchess.trainer.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque

class FindSquareViewModelTest {

    @Test
    fun `starts with a target square`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        assertEquals("e4", viewModel.uiState.value.target?.algebraic)
        assertFalse(viewModel.uiState.value.answered)
        assertNull(viewModel.uiState.value.wasCorrect)
    }

    @Test
    fun `tapping the target is correct`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onSquareTap(sq("e4"))

        val state = viewModel.uiState.value
        assertTrue(state.answered)
        assertEquals(true, state.wasCorrect)
        assertEquals("e4", state.lastAttempt?.algebraic)
        assertEquals(1, state.correctCount)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `tapping another square is incorrect`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onSquareTap(sq("e5"))

        val state = viewModel.uiState.value
        assertTrue(state.answered)
        assertEquals(false, state.wasCorrect)
        assertEquals("e5", state.lastAttempt?.algebraic)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `ignores taps while the miss is shown`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onSquareTap(sq("e5"))
        viewModel.onSquareTap(sq("e4"))

        val state = viewModel.uiState.value
        assertEquals(false, state.wasCorrect)
        assertEquals("e5", state.lastAttempt?.algebraic)
        assertEquals("e4", state.target?.algebraic)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `after a miss the same square can be tried again`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onSquareTap(sq("e5"))
        viewModel.unlockRetry()
        viewModel.onSquareTap(sq("e4"))

        val state = viewModel.uiState.value
        assertEquals("e4", state.target?.algebraic)
        assertEquals(true, state.wasCorrect)
        assertEquals(1, state.correctCount)
        assertEquals(2, state.totalCount)
    }

    @Test
    fun `next question loads a new target`() {
        val viewModel = viewModelWithSquares("e4", "a1")
        viewModel.onSquareTap(sq("e4"))
        viewModel.loadNextQuestion()

        val state = viewModel.uiState.value
        assertEquals("a1", state.target?.algebraic)
        assertFalse(state.answered)
        assertNull(state.wasCorrect)
        assertNull(state.lastAttempt)
        assertEquals(1, state.correctCount)
    }

    private fun viewModelWithSquares(vararg squares: String): FindSquareViewModel {
        val queue = ArrayDeque(squares.map(::sq))
        return FindSquareViewModel(FindSquareDrill { queue.removeFirst() })
    }

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
}
