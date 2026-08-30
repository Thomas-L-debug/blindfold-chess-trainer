package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PieceMovesTest {

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!

    @Test
    fun `bishop moves diagonally only`() {
        assertTrue(PieceType.BISHOP.canMove(sq("a1"), sq("h8")))
        assertTrue(PieceType.BISHOP.canMove(sq("c1"), sq("a3")))
        assertFalse(PieceType.BISHOP.canMove(sq("a1"), sq("a8")))
        assertFalse(PieceType.BISHOP.canMove(sq("a1"), sq("h1")))
        assertFalse(PieceType.BISHOP.canMove(sq("a1"), sq("b3")))
        assertFalse(PieceType.BISHOP.canMove(sq("e4"), sq("e4")))
    }

    @Test
    fun `knight moves in L shape only`() {
        assertTrue(PieceType.KNIGHT.canMove(sq("e4"), sq("f6")))
        assertTrue(PieceType.KNIGHT.canMove(sq("e4"), sq("d6")))
        assertTrue(PieceType.KNIGHT.canMove(sq("e4"), sq("g5")))
        assertTrue(PieceType.KNIGHT.canMove(sq("e4"), sq("c3")))
        assertFalse(PieceType.KNIGHT.canMove(sq("e4"), sq("e6")))
        assertFalse(PieceType.KNIGHT.canMove(sq("e4"), sq("f5")))
        assertFalse(PieceType.KNIGHT.canMove(sq("e4"), sq("e4")))
    }

    @Test
    fun `rook moves on rank or file only`() {
        assertTrue(PieceType.ROOK.canMove(sq("a1"), sq("a8")))
        assertTrue(PieceType.ROOK.canMove(sq("a1"), sq("h1")))
        assertFalse(PieceType.ROOK.canMove(sq("a1"), sq("b2")))
        assertFalse(PieceType.ROOK.canMove(sq("a1"), sq("h8")))
        assertFalse(PieceType.ROOK.canMove(sq("c3"), sq("c3")))
    }

    @Test
    fun `queen combines rook and bishop`() {
        assertTrue(PieceType.QUEEN.canMove(sq("a1"), sq("a8")))
        assertTrue(PieceType.QUEEN.canMove(sq("a1"), sq("h1")))
        assertTrue(PieceType.QUEEN.canMove(sq("a1"), sq("h8")))
        assertFalse(PieceType.QUEEN.canMove(sq("a1"), sq("b3")))
        assertFalse(PieceType.QUEEN.canMove(sq("d4"), sq("d4")))
    }

    @Test
    fun `bishop puzzle stays on the same color`() {
        val squares = ArrayDeque(
            listOf(sq("a1"), sq("a2"), sq("c1")),
        )
        val drill = PiecePathDrill { squares.removeFirst() }
        val puzzle = drill.nextPuzzle(PieceType.BISHOP)

        assertEquals(sq("a1"), puzzle.start)
        assertEquals(sq("c1"), puzzle.target)
        assertEquals(SquareColor.DARK, SquareColor.of(puzzle.start))
        assertEquals(SquareColor.DARK, SquareColor.of(puzzle.target))
    }

    @Test
    fun `puzzle never uses the start square as target`() {
        val squares = ArrayDeque(
            listOf(sq("e4"), sq("e4"), sq("e5")),
        )
        val drill = PiecePathDrill { squares.removeFirst() }
        val puzzle = drill.nextPuzzle(PieceType.ROOK)

        assertEquals(sq("e4"), puzzle.start)
        assertEquals(sq("e5"), puzzle.target)
        assertNotEquals(puzzle.start, puzzle.target)
    }

    @Test
    fun `drill reports legal and illegal moves`() {
        val drill = PiecePathDrill()
        assertTrue(drill.isLegalMove(PieceType.KNIGHT, sq("g1"), sq("f3")))
        assertFalse(drill.isLegalMove(PieceType.KNIGHT, sq("g1"), sq("g3")))
    }
}
