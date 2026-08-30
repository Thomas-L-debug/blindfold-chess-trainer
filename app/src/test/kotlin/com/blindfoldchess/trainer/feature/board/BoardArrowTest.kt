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
}
