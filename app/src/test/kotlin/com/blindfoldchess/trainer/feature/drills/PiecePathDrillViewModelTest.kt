package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.PiecePathDrill
import com.blindfoldchess.trainer.core.chess.PieceType
import com.blindfoldchess.trainer.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PiecePathDrillViewModelTest {

    @Test
    fun `illegal move resets to start on the same puzzle`() {
        val viewModel = viewModelWithFixedPuzzle()
        val start = viewModel.uiState.value.start
        val target = viewModel.uiState.value.target

        viewModel.onFile('f')
        viewModel.onRank(6)

        val state = viewModel.uiState.value
        assertEquals(start, state.current)
        assertEquals(start, state.start)
        assertEquals(target, state.target)
        assertTrue(state.illegal)
        assertFalse(state.solved)
        assertTrue(state.path.isEmpty())
        assertEquals(start, state.lastMoveFrom)
        assertEquals("f6", state.lastMoveSquare?.algebraic)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `legal move to the target solves the puzzle`() {
        val viewModel = viewModelWithFixedPuzzle()

        viewModel.onFile('e')
        viewModel.onRank(5)

        val state = viewModel.uiState.value
        assertTrue(state.solved)
        assertEquals("e5", state.current?.algebraic)
        assertEquals(1, state.correctCount)
        assertEquals(1, state.totalCount)
    }

    @Test
    fun `spoken move must name the selected piece then the square`() {
        val viewModel = viewModelWithFixedPuzzle()
        viewModel.playSpoken(listOf("rook e 5"))

        val state = viewModel.uiState.value
        assertTrue(state.solved)
        assertEquals("e5", state.current?.algebraic)
        assertEquals(1, state.correctCount)
    }

    @Test
    fun `spoken french piece name plays the destination`() {
        val viewModel = viewModelWithFixedPuzzle()
        viewModel.playSpoken(listOf("tour e cinq"))
        assertTrue(viewModel.uiState.value.solved)
        assertEquals("e5", viewModel.uiState.value.current?.algebraic)
    }

    @Test
    fun `spoken square without a piece name uses the selected piece`() {
        val viewModel = viewModelWithFixedPuzzle()
        viewModel.playSpoken(listOf("E5"))

        val state = viewModel.uiState.value
        assertTrue(state.solved)
        assertEquals("e5", state.current?.algebraic)
        assertFalse(state.illegal)
    }

    @Test
    fun `spoken illegal destination is flagged and stays unsolved`() {
        val viewModel = viewModelWithFixedPuzzle()
        viewModel.playSpoken(listOf("rook f6"))

        val state = viewModel.uiState.value
        assertEquals(state.start, state.current)
        assertTrue(state.illegal)
        assertFalse(state.solved)
        assertTrue(state.path.isEmpty())
        assertEquals("rook f6", state.lastSpoken)
        assertEquals(false, state.lastMoveLegal)
    }

    @Test
    fun `unclear spoken instruction is flagged without restarting`() {
        val viewModel = viewModelWithFixedPuzzle()
        val start = viewModel.uiState.value.current
        viewModel.playSpoken(listOf("P5"))

        val state = viewModel.uiState.value
        assertEquals(start, state.current)
        assertFalse(state.illegal)
        assertTrue(state.unrecognized)
        assertFalse(state.solved)
        assertTrue(state.path.isEmpty())
        assertEquals("P5", state.lastSpoken)
    }

    @Test
    fun `spoken wrong piece name resets the path`() {
        val viewModel = viewModelWithFixedPuzzle()
        viewModel.playSpoken(listOf("knight e5"))

        val state = viewModel.uiState.value
        assertEquals(state.start, state.current)
        assertTrue(state.illegal)
        assertFalse(state.solved)
        assertTrue(state.path.isEmpty())
    }

    @Test
    fun `rank is ignored until a file is chosen`() {
        val viewModel = viewModelWithFixedPuzzle()
        val start = viewModel.uiState.value.current

        viewModel.onRank(5)

        assertEquals(start, viewModel.uiState.value.current)
        assertFalse(viewModel.uiState.value.illegal)
        assertFalse(viewModel.uiState.value.solved)
    }

    private fun viewModelWithFixedPuzzle(): PiecePathDrillViewModel {
        val squares = ArrayDeque(listOf(sq("e4"), sq("e5")))
        val drill = PiecePathDrill { squares.removeFirst() }
        return PiecePathDrillViewModel(drill, initialPiece = PieceType.ROOK)
    }

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
}
