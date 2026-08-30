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
