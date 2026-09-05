package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessSpeechParserTest {

    @Test
    fun `pawn pushes`() {
        assertContains("e4", "e4")
        assertContains("e 4", "e4")
        assertContains("e four", "e4")
        assertContains("e quatre", "e4")
        assertContains("S5", "f5")
        assertContains("s 5", "f5")
    }

    @Test
    fun `piece destination in english and french`() {
        assertContains("Nf3", "Nf3")
        assertContains("knight f3", "Nf3")
        assertContains("knight f 3", "Nf3")
        assertContains("knight f three", "Nf3")
        assertContains("knight S5", "Nf5")
        assertContains("cavalier f trois", "Nf3")
        assertContains("bishop c4", "Bc4")
        assertContains("fou c 4", "Bc4")
        assertContains("queen h5", "Qh5")
        assertContains("dame h 5", "Qh5")
        assertContains("rook d1", "Rd1")
        assertContains("tour d 1", "Rd1")
        assertContains("king e2", "Ke2")
        assertContains("roi e 2", "Ke2")
    }

    @Test
    fun `captures`() {
        assertContains("knight takes f3", "Nxf3")
        assertContains("cavalier prend f3", "Nxf3")
        assertContains("e takes d5", "exd5")
        assertContains("exd5", "exd5")
        assertContains("bishop takes c4", "Bxc4")
        assertContains("pawn takes F4", "exf4")
        assertContains("pawn takes F4", "gxf4")
        assertContains("pawn takes f 4", "exf4")
        assertContains("pion prend F4", "exf4")
        assertContains("pion prend f4", "gxf4")
    }

    @Test
    fun `castling`() {
        assertContains("O-O", "O-O")
        assertContains("castle", "O-O")
        assertContains("petit roque", "O-O")
        assertContains("petit rock", "O-O")
        assertContains("short castle", "O-O")
        assertContains("O-O-O", "O-O-O")
        assertContains("grand roque", "O-O-O")
        assertContains("grand rock", "O-O-O")
        assertContains("long castle", "O-O-O")
        assertContains("queenside castle", "O-O-O")
    }

    @Test
    fun `from to squares become uci`() {
        assertContains("e2 e4", "e2e4")
        assertContains("e2 to e4", "e2e4")
        assertContains("g1 f3", "g1f3")
    }

    @Test
    fun `origin file disambiguation`() {
        assertContains("knight b d 2", "Nbd2")
        assertContains("Nbd2", "Nbd2")
    }

    @Test
    fun `path move needs a piece name and a square`() {
        assertPath("cavalier f 3", PieceType.KNIGHT, "f3")
        assertPath("knight f3", PieceType.KNIGHT, "f3")
        assertPath("fou e 5", PieceType.BISHOP, "e5")
        assertPath("bishop e5", PieceType.BISHOP, "e5")
        assertPath("tour a1", PieceType.ROOK, "a1")
        assertPath("rook a 1", PieceType.ROOK, "a1")
        assertPath("dame h8", PieceType.QUEEN, "h8")
        assertPath("queen h 8", PieceType.QUEEN, "h8")

        val squareOnly = ChessSpeechParser.parsePathMove("f3")
        assertNull(squareOnly.piece)
        assertEquals("f3", squareOnly.square?.algebraic)

        val uppercaseSquare = ChessSpeechParser.parsePathMove("H6")
        assertNull(uppercaseSquare.piece)
        assertEquals("h6", uppercaseSquare.square?.algebraic)

        val essAsF = ChessSpeechParser.parsePathMove("S5")
        assertNull(essAsF.piece)
        assertEquals("f5", essAsF.square?.algebraic)

        val pieceOnly = ChessSpeechParser.parsePathMove("cavalier")
        assertEquals(PieceType.KNIGHT, pieceOnly.piece)
        assertNull(pieceOnly.square)
    }

    @Test
    fun `empty and noise produce nothing useful or stay empty`() {
        assertTrue(ChessSpeechParser.candidates("").isEmpty())
        assertTrue(ChessSpeechParser.candidates("   ").isEmpty())
    }

    private fun assertPath(utterance: String, piece: PieceType, square: String) {
        val parsed = ChessSpeechParser.parsePathMove(utterance)
        assertEquals("piece for \"$utterance\"", piece, parsed.piece)
        assertEquals("square for \"$utterance\"", square, parsed.square?.algebraic)
    }

    private fun assertContains(utterance: String, expected: String) {
        val found = ChessSpeechParser.candidates(utterance)
        assertTrue(
            "expected $expected in $found for \"$utterance\"",
            found.contains(expected),
        )
    }
}
