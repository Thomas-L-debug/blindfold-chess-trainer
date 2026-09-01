package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFollowDrillTest {

    private val drill = GameFollowDrill()

    @Test
    fun `every catalog game parses with matching token counts`() {
        FamousGamesCatalog.games.forEach { game ->
            val loaded = drill.load(game.id)
            assertEquals(game.id, loaded.game.id)
            assertEquals(game.plyCount, loaded.moves.size)
            assertTrue("${game.id} should have moves", loaded.moves.isNotEmpty())
        }
    }

    @Test
    fun `opera game first move is e2-e4`() {
        val first = drill.load("opera").moves.first()
        assertEquals("e4", first.san)
        assertEquals("e2", first.from.algebraic)
        assertEquals("e4", first.to.algebraic)
        assertTrue(first.isWhite)
        assertEquals("1. e4", first.prompt)
    }

    @Test
    fun `opera queenside castle is e1-c1`() {
        val castle = drill.load("opera").moves.first { it.san == "O-O-O" }
        assertEquals("e1", castle.from.algebraic)
        assertEquals("c1", castle.to.algebraic)
        assertEquals(12, castle.moveNumber)
    }

    @Test
    fun `opera mate is rook d1 to d8`() {
        val last = drill.load("opera").moves.last()
        assertEquals("Rd8#", last.san)
        assertEquals("d1", last.from.algebraic)
        assertEquals("d8", last.to.algebraic)
        assertFalse(drill.isExpected(last, Square('d', 1), Square('d', 7)))
        assertTrue(drill.isExpected(last, Square('d', 1), Square('d', 8)))
    }

    @Test
    fun `reti queen sacrifice is d3-d8`() {
        val sacrifice = drill.load("reti-tartakower").moves.first { it.san == "Qd8+" }
        assertEquals("d3", sacrifice.from.algebraic)
        assertEquals("d8", sacrifice.to.algebraic)
    }

    @Test
    fun `game of the century starts Nf3 and ends Rc2 mate`() {
        val moves = drill.load("game-of-the-century").moves
        assertEquals("Nf3", moves.first().san)
        assertEquals("g1", moves.first().from.algebraic)
        assertEquals("f3", moves.first().to.algebraic)
        val last = moves.last()
        assertEquals("Rc2#", last.san)
        assertEquals("a2", last.from.algebraic)
        assertEquals("c2", last.to.algebraic)
        assertFalse(last.isWhite)
    }

    @Test
    fun `played moves format pairs white and black`() {
        val moves = drill.load("legal").moves
        assertEquals("", formatPlayedMoves(moves, 0))
        assertEquals("1. e4", formatPlayedMoves(moves, 1))
        assertEquals("1. e4 e5", formatPlayedMoves(moves, 2))
        assertEquals("1. e4 e5  2. Nf3", formatPlayedMoves(moves, 3))
    }

    @Test
    fun `positions start with 32 pieces and follow each move`() {
        val loaded = drill.load("opera")
        assertEquals(loaded.moves.size + 1, loaded.positions.size)
        val start = loaded.positions.first()
        assertEquals(32, start.size)
        assertTrue(start.any { it.square.algebraic == "e2" && it.man == ChessMan.PAWN && it.isWhite })
        assertTrue(start.any { it.square.algebraic == "e7" && it.man == ChessMan.PAWN && !it.isWhite })

        val afterE4 = loaded.positions[1]
        assertTrue(afterE4.any { it.square.algebraic == "e4" && it.man == ChessMan.PAWN && it.isWhite })
        assertFalse(afterE4.any { it.square.algebraic == "e2" })
        assertEquals(32, afterE4.size)
    }
}
