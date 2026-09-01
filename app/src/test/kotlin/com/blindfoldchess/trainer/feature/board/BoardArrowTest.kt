package com.blindfoldchess.trainer.feature.board

import com.blindfoldchess.trainer.core.chess.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardArrowTest {

    @Test
    fun `no arrows until at least one step`() {
        assertTrue(arrowsAlong(null, emptyList()).isEmpty())
        assertTrue(arrowsAlong(Square('e', 2), emptyList()).isEmpty())
    }

    @Test
    fun `path becomes consecutive arrows from start`() {
        val arrows = arrowsAlong(
            Square('e', 2),
            listOf(Square('e', 4), Square('e', 8)),
        )
        assertEquals(
            listOf(
                BoardArrow(Square('e', 2), Square('e', 4)),
                BoardArrow(Square('e', 4), Square('e', 8)),
            ),
            arrows,
        )
    }

    @Test
    fun `visited squares include start and every stop`() {
        val arrows = arrowsAlong(
            Square('e', 2),
            listOf(Square('e', 4), Square('e', 8)),
        )
        assertEquals(
            listOf(Square('e', 2), Square('e', 4), Square('e', 8)),
            visitedSquares(arrows),
        )
        assertTrue(visitedSquares(emptyList()).isEmpty())
    }

    @Test
    fun `grid tap maps to algebraic squares`() {
        assertEquals(Square('a', 8), squareFromGrid(0, 0))
        assertEquals(Square('h', 1), squareFromGrid(7, 7))
        assertEquals(Square('e', 4), squareFromGrid(4, 4))
        assertEquals(null, squareFromGrid(-1, 0))
        assertEquals(null, squareFromGrid(8, 0))
    }

    @Test
    fun `flipped grid tap maps from black's side`() {
        assertEquals(Square('h', 1), squareFromGrid(0, 0, flipped = true))
        assertEquals(Square('a', 8), squareFromGrid(7, 7, flipped = true))
        assertEquals(Square('e', 4), squareFromGrid(3, 3, flipped = true))
        assertEquals(Square('d', 5), squareFromGrid(4, 4, flipped = true))
    }
}
