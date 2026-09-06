package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Test

class ChessMoveAnnouncerTest {

    @Test
    fun `pawn push`() {
        assertEquals("E four", en("e4"))
        assertEquals("E quatre", fr("e4"))
    }

    @Test
    fun `piece move`() {
        assertEquals("knight F three", en("Nf3"))
        assertEquals("cavalier F trois", fr("Nf3"))
    }

    @Test
    fun `captures`() {
        assertEquals("knight takes F three", en("Nxf3"))
        assertEquals("cavalier prend F trois", fr("Nxf3"))
        assertEquals("E takes D five", en("exd5"))
        assertEquals("E prend D cinq", fr("exd5"))
    }

    @Test
    fun `castling`() {
        assertEquals("castle", en("O-O"))
        assertEquals("petit roque", fr("O-O"))
        assertEquals("long castle", en("O-O-O"))
        assertEquals("grand roque", fr("O-O-O"))
    }

    @Test
    fun `check and mate`() {
        assertEquals("queen H four check", en("Qh4+"))
        assertEquals("dame H quatre échec", fr("Qh4+"))
        assertEquals("E eight queen mate", en("e8=Q#"))
        assertEquals("E huit dame mat", fr("e8=Q#"))
    }

    @Test
    fun `disambiguation`() {
        assertEquals("knight B D two", en("Nbd2"))
        assertEquals("cavalier B D deux", fr("Nbd2"))
    }

    private fun en(san: String) = ChessMoveAnnouncer.spoken(san, ChessMoveAnnouncer.Language.English)

    private fun fr(san: String) = ChessMoveAnnouncer.spoken(san, ChessMoveAnnouncer.Language.French)
}
