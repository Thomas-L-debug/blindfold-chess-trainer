package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.core.chess.ChessSession
import com.blindfoldchess.trainer.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeBoardViewModelTest {

    @Test
    fun `starts with the initial position`() {
        val viewModel = FreeBoardViewModel()
        val state = viewModel.uiState.value
        assertEquals(32, state.pieces.size)
        assertTrue(state.isWhiteToMove)
        assertTrue(state.moves.isEmpty())
        assertNull(state.fromSquare)
        assertNull(state.selectedMan)
    }

    @Test
    fun `tapping a piece then a legal square plays the move`() {
        val viewModel = FreeBoardViewModel()
        viewModel.onSquareTap(sq("e2"))
        assertEquals("e2", viewModel.uiState.value.fromSquare?.algebraic)

        viewModel.onSquareTap(sq("e4"))

        val state = viewModel.uiState.value
        assertNull(state.fromSquare)
        assertEquals(1, state.moves.size)
        assertEquals("e4", state.moves.first().san)
        assertFalse(state.isWhiteToMove)
        assertTrue(state.pieces.any { it.square.algebraic == "e4" && it.man == ChessMan.PAWN && it.isWhite })
        assertEquals(true, state.lastAttemptCorrect)
    }

    @Test
    fun `illegal destination flashes and stays in the same position`() {
        val viewModel = FreeBoardViewModel()
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("e5"))

        val state = viewModel.uiState.value
        assertEquals(false, state.lastAttemptCorrect)
        assertEquals("e2", state.lastAttemptFrom?.algebraic)
        assertEquals("e5", state.lastAttemptSquare?.algebraic)
        assertTrue(state.moves.isEmpty())
        assertTrue(state.isWhiteToMove)
        assertNull(state.fromSquare)
    }

    @Test
    fun `illegal pawn pad move keeps the attempted origin and destination`() {
        val viewModel = FreeBoardViewModel()
        viewModel.onFile('e')
        viewModel.onRank(5)

        val state = viewModel.uiState.value
        assertEquals(false, state.lastAttemptCorrect)
        assertEquals("e2", state.lastAttemptFrom?.algebraic)
        assertEquals("e5", state.lastAttemptSquare?.algebraic)
    }

    @Test
    fun `piece type then destination plays a unique move`() {
        val viewModel = FreeBoardViewModel()
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.onFile('f')
        viewModel.onRank(3)

        val state = viewModel.uiState.value
        assertEquals("Nf3", state.moves.single().san)
        assertNull(state.selectedMan)
        assertTrue(state.pieces.any { it.square.algebraic == "f3" && it.man == ChessMan.KNIGHT && it.isWhite })
    }

    @Test
    fun `ambiguous piece destination asks for the origin file`() {
        val viewModel = FreeBoardViewModel(
            ChessSession("4k3/8/8/8/8/2N1N3/8/4K3 w - - 0 1"),
        )
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.onFile('d')
        viewModel.onRank(5)

        val waiting = viewModel.uiState.value
        assertTrue(waiting.moves.isEmpty())
        assertEquals(listOf("c", "e"), waiting.disambiguation?.options)
        assertTrue(waiting.disambiguation?.askFile == true)

        viewModel.onFile('c')
        val played = viewModel.uiState.value
        assertEquals(1, played.moves.size)
        assertEquals("c3", played.moves.single().from.algebraic)
        assertEquals("d5", played.moves.single().to.algebraic)
        assertNull(played.disambiguation)
    }

    @Test
    fun `two knights capturing the same square ask for the file`() {
        val viewModel = FreeBoardViewModel(
            ChessSession("4k3/8/8/3p4/8/2N1N3/8/4K3 w - - 0 1"),
        )
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.onFile('d')
        viewModel.onRank(5)

        val waiting = viewModel.uiState.value
        assertTrue(waiting.moves.isEmpty())
        assertNull(waiting.lastAttemptCorrect)
        assertEquals(listOf("c", "e"), waiting.disambiguation?.options)
    }

    @Test
    fun `x then destination captures with the selected piece`() {
        val viewModel = FreeBoardViewModel(
            ChessSession("4k3/8/8/4p3/8/5N2/8/4K3 w - - 0 1"),
        )
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.toggleCapture()
        viewModel.onFile('e')
        viewModel.onRank(5)

        val state = viewModel.uiState.value
        assertEquals(1, state.moves.size)
        assertEquals("e5", state.moves.single().to.algebraic)
        assertFalse(state.capturing)
        assertTrue(state.pieces.none { it.square.algebraic == "e5" && !it.isWhite })
    }

    @Test
    fun `kingside castle button plays O-O`() {
        val session = ChessSession()
        "e4 e5 Nf3 Nc6 Bc4 Bc5".split(" ").forEach { session.playSan(it) }
        val viewModel = FreeBoardViewModel(session)
        viewModel.castle(kingside = true)

        val state = viewModel.uiState.value
        assertTrue(state.moves.last().san == "O-O" || state.moves.last().san == "0-0")
        assertTrue(state.pieces.any { it.square.algebraic == "g1" && it.man == ChessMan.KING && it.isWhite })
    }

    @Test
    fun `player can specify origin file before destination like Raxd1`() {
        val viewModel = FreeBoardViewModel(
            ChessSession("4k3/8/8/8/8/8/8/R2rR2K w - - 0 1"),
        )
        viewModel.selectMan(ChessMan.ROOK)
        viewModel.onFile('a')
        viewModel.toggleCapture()
        assertEquals('a', viewModel.uiState.value.originFile)
        assertTrue(viewModel.uiState.value.capturing)
        viewModel.onFile('d')
        viewModel.onRank(1)

        val state = viewModel.uiState.value
        assertEquals("a1", state.moves.single().from.algebraic)
        assertEquals("d1", state.moves.single().to.algebraic)
    }

    @Test
    fun `player can specify origin rank before destination like N3xe5`() {
        val viewModel = FreeBoardViewModel(
            ChessSession("4k3/8/8/4p3/2N5/5N2/8/4K3 w - - 0 1"),
        )
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.onRank(3)
        viewModel.toggleCapture()
        viewModel.onFile('e')
        viewModel.onRank(5)

        val state = viewModel.uiState.value
        assertEquals("f3", state.moves.single().from.algebraic)
        assertEquals("e5", state.moves.single().to.algebraic)
    }

    @Test
    fun `player can specify origin file then destination like Nbd2`() {
        val viewModel = FreeBoardViewModel(
            ChessSession("4k3/8/8/8/8/1N3N2/8/4K3 w - - 0 1"),
        )
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.onFile('b')
        viewModel.onFile('d')
        viewModel.onRank(2)

        val state = viewModel.uiState.value
        assertEquals("b3", state.moves.single().from.algebraic)
        assertEquals("d2", state.moves.single().to.algebraic)
    }

    @Test
    fun `destination without a piece type plays a pawn`() {
        val viewModel = FreeBoardViewModel()
        viewModel.onFile('e')
        viewModel.onRank(4)

        val state = viewModel.uiState.value
        assertEquals("e4", state.moves.single().san)
        assertTrue(state.pieces.any { it.square.algebraic == "e4" && it.man == ChessMan.PAWN && it.isWhite })
    }

    @Test
    fun `selecting the same piece again clears it`() {
        val viewModel = FreeBoardViewModel()
        viewModel.selectMan(ChessMan.KNIGHT)
        assertEquals(ChessMan.KNIGHT, viewModel.uiState.value.selectedMan)
        viewModel.selectMan(ChessMan.KNIGHT)
        assertNull(viewModel.uiState.value.selectedMan)
    }

    @Test
    fun `navigation can walk the line and a new move discards the rest`() {
        val viewModel = FreeBoardViewModel()
        viewModel.onFile('e')
        viewModel.onRank(4)
        viewModel.onFile('e')
        viewModel.onRank(5)
        viewModel.selectMan(ChessMan.KNIGHT)
        viewModel.onFile('f')
        viewModel.onRank(3)

        viewModel.stepBack()
        viewModel.stepBack()
        val reviewing = viewModel.uiState.value
        assertEquals(1, reviewing.plyIndex)
        assertEquals(3, reviewing.lineLength)
        assertTrue(reviewing.reviewing)
        assertTrue(reviewing.canStepForward)

        viewModel.onFile('c')
        viewModel.onRank(5)
        val changed = viewModel.uiState.value
        assertEquals(listOf("e4", "c5"), changed.moves.map { it.san })
        assertEquals(2, changed.lineLength)
        assertFalse(changed.canStepForward)
    }

    @Test
    fun `undo and reset restore earlier positions`() {
        val viewModel = FreeBoardViewModel()
        viewModel.onFile('e')
        viewModel.onRank(4)
        viewModel.onFile('e')
        viewModel.onRank(5)

        viewModel.undo()
        assertEquals(1, viewModel.uiState.value.moves.size)
        assertFalse(viewModel.uiState.value.isWhiteToMove)

        viewModel.reset()
        val state = viewModel.uiState.value
        assertTrue(state.moves.isEmpty())
        assertTrue(state.isWhiteToMove)
        assertEquals(32, state.pieces.size)
    }

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
}
