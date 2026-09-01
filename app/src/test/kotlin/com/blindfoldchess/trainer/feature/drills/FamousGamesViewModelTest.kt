package com.blindfoldchess.trainer.feature.drills

import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.core.chess.FamousGame
import com.blindfoldchess.trainer.core.chess.GameFollowDrill
import com.blindfoldchess.trainer.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamousGamesViewModelTest {

    @Test
    fun `starts in the library`() {
        val viewModel = viewModel()
        val state = viewModel.uiState.value
        assertTrue(state.browsing)
        assertEquals(1, state.games.size)
        assertNull(state.game)
        assertTrue(state.pieces.isEmpty())
    }

    @Test
    fun `selecting a game shows the starting position and first move`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")

        val state = viewModel.uiState.value
        assertFalse(state.browsing)
        assertEquals("Scholar's Mate", state.game?.title)
        assertEquals("1. e4", state.currentMove?.prompt)
        assertEquals(4, state.moves.size)
        assertEquals(32, state.pieces.size)
        assertTrue(state.pieces.any { it.square.algebraic == "e2" && it.man == ChessMan.PAWN && it.isWhite })
        assertFalse(state.completed)
        assertTrue(state.boardTappable)
    }

    @Test
    fun `tapping the piece then the destination plays the move`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")

        play(viewModel, "e2", "e4")

        val state = viewModel.uiState.value
        assertEquals(1, state.plyIndex)
        assertEquals("1... e5", state.currentMove?.prompt)
        assertEquals("e2", state.lastPlayedFrom?.algebraic)
        assertEquals("e4", state.lastPlayedTo?.algebraic)
        assertEquals(1, state.correctCount)
        assertEquals(1, state.totalCount)
        assertTrue(state.lastAttemptCorrect == true)
        assertNull(state.fromSquare)
        assertTrue(state.pieces.any { it.square.algebraic == "e4" && it.man == ChessMan.PAWN && it.isWhite })
        assertFalse(state.pieces.any { it.square.algebraic == "e2" })
    }

    @Test
    fun `wrong destination stays on the same move`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")

        play(viewModel, "e2", "e3")

        val state = viewModel.uiState.value
        assertEquals(0, state.plyIndex)
        assertEquals("1. e4", state.currentMove?.prompt)
        assertEquals(false, state.lastAttemptCorrect)
        assertEquals("e2", state.lastAttemptFrom?.algebraic)
        assertEquals("e3", state.lastAttemptSquare?.algebraic)
        assertEquals(0, state.correctCount)
        assertEquals(0, state.totalCount)
        assertEquals(1, state.attemptsOnCurrent)
        assertNull(state.fromSquare)
    }

    @Test
    fun `retry after a miss still completes the ply without first-try credit`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")

        play(viewModel, "e2", "e3")
        play(viewModel, "e2", "e4")

        val state = viewModel.uiState.value
        assertEquals(1, state.plyIndex)
        assertEquals(0, state.correctCount)
        assertEquals(1, state.totalCount)
        assertEquals(0, state.attemptsOnCurrent)
    }

    @Test
    fun `empty square tap is ignored until a piece is selected`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        viewModel.onSquareTap(sq("e4"))

        assertNull(viewModel.uiState.value.fromSquare)
        assertEquals(0, viewModel.uiState.value.plyIndex)
    }

    @Test
    fun `opponent piece tap is ignored until a piece is selected`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        viewModel.onSquareTap(sq("e7"))

        assertNull(viewModel.uiState.value.fromSquare)
    }

    @Test
    fun `tapping the selected piece again deselects it`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        viewModel.onSquareTap(sq("e2"))
        assertEquals("e2", viewModel.uiState.value.fromSquare?.algebraic)

        viewModel.onSquareTap(sq("e2"))
        assertNull(viewModel.uiState.value.fromSquare)
    }

    @Test
    fun `tapping another piece of the side to move reselects`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        viewModel.onSquareTap(sq("e2"))
        viewModel.onSquareTap(sq("g1"))

        assertEquals("g1", viewModel.uiState.value.fromSquare?.algebraic)
        assertEquals(0, viewModel.uiState.value.plyIndex)
    }

    @Test
    fun `playing every move completes the game`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")

        play(viewModel, "e2", "e4")
        play(viewModel, "e7", "e5")
        play(viewModel, "d1", "h5")
        play(viewModel, "b8", "c6")

        val state = viewModel.uiState.value
        assertTrue(state.completed)
        assertNull(state.currentMove)
        assertEquals(4, state.correctCount)
        assertEquals(4, state.totalCount)
        assertEquals("1. e4 e5  2. Qh5 Nc6", state.playedMoves)
        assertFalse(state.boardTappable)
        assertTrue(state.pieces.any { it.square.algebraic == "h5" && it.man == ChessMan.QUEEN && it.isWhite })
    }

    @Test
    fun `cannot step forward until the next move has been played`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")

        viewModel.stepForward()
        viewModel.goToLatest()

        val state = viewModel.uiState.value
        assertEquals(0, state.plyIndex)
        assertFalse(state.canStepForward)
        assertFalse(state.canGoToLatest)
        assertFalse(state.canStepBack)
    }

    @Test
    fun `stepping back restores the previous position`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        play(viewModel, "e2", "e4")

        viewModel.stepBack()

        val state = viewModel.uiState.value
        assertEquals(0, state.plyIndex)
        assertEquals(1, state.maxProgressPly)
        assertTrue(state.reviewing)
        assertFalse(state.boardTappable)
        assertTrue(state.canStepForward)
        assertTrue(state.canGoToLatest)
        assertFalse(state.canStepBack)
        assertTrue(state.pieces.any { it.square.algebraic == "e2" && it.man == ChessMan.PAWN && it.isWhite })
        assertNull(state.lastPlayedFrom)
        assertEquals(1, state.correctCount)
    }

    @Test
    fun `stepping forward is allowed only onto already practiced moves`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        play(viewModel, "e2", "e4")
        viewModel.stepBack()
        viewModel.stepForward()

        val afterForward = viewModel.uiState.value
        assertEquals(1, afterForward.plyIndex)
        assertTrue(afterForward.boardTappable)
        assertEquals("1... e5", afterForward.currentMove?.prompt)

        viewModel.stepForward()
        assertEquals(1, viewModel.uiState.value.plyIndex)
    }

    @Test
    fun `start and latest jump across practiced moves`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        play(viewModel, "e2", "e4")
        play(viewModel, "e7", "e5")

        viewModel.goToStart()
        val atStart = viewModel.uiState.value
        assertEquals(0, atStart.plyIndex)
        assertEquals(2, atStart.maxProgressPly)
        assertTrue(atStart.reviewing)
        assertEquals("", atStart.playedMoves)

        viewModel.stepForward()
        assertEquals(1, viewModel.uiState.value.plyIndex)

        viewModel.goToLatest()
        val atLatest = viewModel.uiState.value
        assertEquals(2, atLatest.plyIndex)
        assertTrue(atLatest.atFrontier)
        assertTrue(atLatest.boardTappable)
        assertEquals("2. Qh5", atLatest.currentMove?.prompt)
        assertEquals(2, atLatest.correctCount)
    }

    @Test
    fun `taps are ignored while reviewing an earlier position`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        play(viewModel, "e2", "e4")
        viewModel.stepBack()
        play(viewModel, "e2", "e4")

        val state = viewModel.uiState.value
        assertEquals(0, state.plyIndex)
        assertTrue(state.reviewing)
    }

    @Test
    fun `back to library resets the session`() {
        val viewModel = viewModel()
        viewModel.selectGame("scholar")
        play(viewModel, "e2", "e4")
        viewModel.backToLibrary()

        val state = viewModel.uiState.value
        assertTrue(state.browsing)
        assertNull(state.game)
        assertTrue(state.moves.isEmpty())
        assertTrue(state.pieces.isEmpty())
        assertEquals(0, state.correctCount)
    }

    private fun viewModel(): FamousGamesViewModel {
        val game = FamousGame(
            id = "scholar",
            title = "Scholar's Mate",
            white = "White",
            black = "Black",
            event = "Test",
            year = 2000,
            result = "1-0",
            san = "e4 e5 Qh5 Nc6",
        )
        return FamousGamesViewModel(GameFollowDrill(listOf(game)))
    }

    private fun play(viewModel: FamousGamesViewModel, from: String, to: String) {
        viewModel.onSquareTap(sq(from))
        viewModel.onSquareTap(sq(to))
    }

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!
}
