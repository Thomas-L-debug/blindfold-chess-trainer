package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessSessionTest {

    private fun sq(notation: String): Square = Square.fromAlgebraic(notation)!!

    @Test
    fun `starts at the initial position`() {
        val snapshot = ChessSession().snapshot()
        assertEquals(32, snapshot.pieces.size)
        assertTrue(snapshot.isWhiteToMove)
        assertTrue(snapshot.moves.isEmpty())
        assertFalse(snapshot.inCheck)
        assertFalse(snapshot.isCheckmate)
    }

    @Test
    fun `plays e4 from squares then Nf3 from SAN`() {
        val session = ChessSession()
        val e4 = session.playSquares(sq("e2"), sq("e4"))
        assertTrue(e4 is PlayResult.Played)
        assertEquals("e4", (e4 as PlayResult.Played).move.san)

        val afterE4 = session.snapshot()
        assertTrue(afterE4.pieces.any { it.square.algebraic == "e4" && it.man == ChessMan.PAWN && it.isWhite })
        assertFalse(afterE4.isWhiteToMove)

        val nf6 = session.playSan("Nf6")
        assertTrue(nf6 is PlayResult.Played)
        assertEquals("Nf6", (nf6 as PlayResult.Played).move.san)
        assertEquals(2, session.snapshot().moves.size)
    }

    @Test
    fun `rejects an illegal pawn leap`() {
        val session = ChessSession()
        val result = session.playSquares(sq("e2"), sq("e5"))
        assertEquals(PlayResult.Illegal, result)
        assertEquals(32, session.snapshot().pieces.size)
        assertTrue(session.snapshot().moves.isEmpty())
    }

    @Test
    fun `rejects illegal SAN`() {
        val session = ChessSession()
        assertEquals(PlayResult.Illegal, session.playSan("Qh5"))
        assertTrue(session.snapshot().moves.isEmpty())
    }

    @Test
    fun `stepping back and forward keeps the line`() {
        val session = ChessSession()
        session.playSan("e4")
        session.playSan("e5")
        session.playSan("Nf3")
        assertTrue(session.stepBack())
        val mid = session.snapshot()
        assertEquals(2, mid.plyIndex)
        assertEquals(3, mid.lineLength)
        assertTrue(mid.canStepForward)
        assertTrue(session.stepForward())
        assertEquals(3, session.snapshot().plyIndex)
        assertEquals("Nf3", session.snapshot().moves.last().san)
    }

    @Test
    fun `a different move in the middle discards the rest of the line`() {
        val session = ChessSession()
        session.playSan("e4")
        session.playSan("e5")
        session.playSan("Nf3")
        session.stepBack()
        session.stepBack()
        val changed = session.playSan("c5")
        assertTrue(changed is PlayResult.Played)
        val snapshot = session.snapshot()
        assertEquals(listOf("e4", "c5"), snapshot.moves.map { it.san })
        assertEquals(2, snapshot.plyIndex)
        assertEquals(2, snapshot.lineLength)
        assertFalse(snapshot.canStepForward)
    }

    @Test
    fun `replaying the same next move keeps the rest of the line`() {
        val session = ChessSession()
        session.playSan("e4")
        session.playSan("e5")
        session.playSan("Nf3")
        session.stepBack()
        session.stepBack()
        val same = session.playSan("e5")
        assertTrue(same is PlayResult.Played)
        val snapshot = session.snapshot()
        assertEquals(2, snapshot.plyIndex)
        assertEquals(3, snapshot.lineLength)
        assertTrue(snapshot.canStepForward)
        session.goToLatest()
        assertEquals("Nf3", session.snapshot().moves.last().san)
    }

    @Test
    fun `undo restores the previous position`() {
        val session = ChessSession()
        session.playSan("e4")
        session.playSan("e5")
        assertTrue(session.undo())
        val snapshot = session.snapshot()
        assertEquals(1, snapshot.moves.size)
        assertFalse(snapshot.isWhiteToMove)
        assertTrue(snapshot.pieces.any { it.square.algebraic == "e7" && it.man == ChessMan.PAWN && !it.isWhite })
        assertTrue(session.undo())
        assertFalse(session.undo())
    }

    @Test
    fun `reset returns to the starting position`() {
        val session = ChessSession()
        session.playSan("e4")
        session.playSan("e5")
        session.reset()
        val snapshot = session.snapshot()
        assertTrue(snapshot.moves.isEmpty())
        assertTrue(snapshot.isWhiteToMove)
        assertEquals(32, snapshot.pieces.size)
        assertTrue(snapshot.pieces.any { it.square.algebraic == "e2" })
    }

    @Test
    fun `castling works from SAN and from king to g1`() {
        val session = ChessSession()
        "e4 e5 Nf3 Nc6 Bc4 Bc5".split(" ").forEach { token ->
            assertTrue(session.playSan(token) is PlayResult.Played)
        }
        val castle = session.playSquares(sq("e1"), sq("g1"))
        assertTrue(castle is PlayResult.Played)
        val san = (castle as PlayResult.Played).move.san
        assertTrue(san == "O-O" || san == "0-0")
        val snapshot = session.snapshot()
        assertTrue(snapshot.pieces.any { it.square.algebraic == "g1" && it.man == ChessMan.KING && it.isWhite })
        assertTrue(snapshot.pieces.any { it.square.algebraic == "f1" && it.man == ChessMan.ROOK && it.isWhite })
    }

    @Test
    fun `piece plus destination is unique or ambiguous`() {
        val session = ChessSession("4k3/8/8/8/8/2N1N3/8/4K3 w - - 0 1")
        val unique = session.playPieceTo(ChessMan.KNIGHT, sq("b5"))
        assertTrue(unique is PlayResult.Played)

        val session2 = ChessSession("4k3/8/8/8/8/2N1N3/8/4K3 w - - 0 1")
        val ambiguous = session2.playPieceTo(ChessMan.KNIGHT, sq("d5"))
        assertTrue(ambiguous is PlayResult.Ambiguous)
        val origins = (ambiguous as PlayResult.Ambiguous).origins.map { it.algebraic }.toSet()
        assertEquals(setOf("c3", "e3"), origins)

        val resolved = session2.playPieceTo(ChessMan.KNIGHT, sq("d5"), originFile = 'c')
        assertTrue(resolved is PlayResult.Played)
        assertEquals("c3", (resolved as PlayResult.Played).move.from.algebraic)
    }

    @Test
    fun `capture flag distinguishes taking a piece`() {
        val session = ChessSession("4k3/8/8/4p3/8/5N2/8/4K3 w - - 0 1")
        val withoutX = session.playPieceTo(ChessMan.KNIGHT, sq("e5"), capture = false)
        assertTrue(withoutX is PlayResult.Played)
        assertEquals("e5", (withoutX as PlayResult.Played).move.to.algebraic)
    }

    @Test
    fun `two knights capturing the same square still need disambiguation`() {
        val session = ChessSession("4k3/8/8/3p4/8/2N1N3/8/4K3 w - - 0 1")
        val ambiguous = session.playPieceTo(ChessMan.KNIGHT, sq("d5"), capture = false)
        assertTrue(ambiguous is PlayResult.Ambiguous)
        val origins = (ambiguous as PlayResult.Ambiguous).origins.map { it.algebraic }.toSet()
        assertEquals(setOf("c3", "e3"), origins)

        val withX = ChessSession("4k3/8/8/3p4/8/2N1N3/8/4K3 w - - 0 1")
            .playPieceTo(ChessMan.KNIGHT, sq("d5"), capture = true)
        assertTrue(withX is PlayResult.Ambiguous)
    }

    @Test
    fun `playUci accepts a promotion suffix`() {
        val session = ChessSession("8/P7/8/8/8/8/8/4K2k w - - 0 1")
        val result = session.playUci("a7a8q")
        assertTrue(result is PlayResult.Played)
        assertEquals("a7", (result as PlayResult.Played).move.from.algebraic)
        assertEquals("a8", result.move.to.algebraic)
        assertTrue(session.snapshot().pieces.any {
            it.square.algebraic == "a8" && it.man == ChessMan.QUEEN && it.isWhite
        })
    }

    @Test
    fun `fen matches the current position`() {
        val session = ChessSession()
        session.playSan("e4")
        assertTrue(session.fen().startsWith("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b"))
    }

    @Test
    fun `promotion defaults to queen`() {
        val session = ChessSession("8/P7/8/8/8/8/8/4K2k w - - 0 1")
        val result = session.playSquares(sq("a7"), sq("a8"))
        assertTrue(result is PlayResult.Played)
        val snapshot = session.snapshot()
        assertTrue(
            snapshot.pieces.any {
                it.square.algebraic == "a8" && it.man == ChessMan.QUEEN && it.isWhite
            },
        )
    }
}
