package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotationTutorialTest {

    @Test
    fun `spoken examples are announced in English and French`() {
        assertTrue(NotationTutorial.spokenSans.isNotEmpty())
        for (san in NotationTutorial.spokenSans) {
            val english = ChessMoveAnnouncer.spoken(san, ChessMoveAnnouncer.Language.English)
            val french = ChessMoveAnnouncer.spoken(san, ChessMoveAnnouncer.Language.French)
            assertTrue("$san should have an English phrase", english.isNotBlank())
            assertTrue("$san should have a French phrase", french.isNotBlank())
            assertFalse(
                "$san English fell back to letter-by-letter",
                isLetterized(san, english),
            )
            assertFalse(
                "$san French fell back to letter-by-letter",
                isLetterized(san, french),
            )
        }
    }

    @Test
    fun `occupancy label follows English and French word order`() {
        val piece = OccupiedSquare(Square('g', 1), ChessMan.KNIGHT, isWhite = true)
        assertEquals(
            "White knight",
            NotationTutorial.occupancyLabel(piece, ChessMoveAnnouncer.Language.English),
        )
        assertEquals(
            "cavalier blanc",
            NotationTutorial.occupancyLabel(piece, ChessMoveAnnouncer.Language.French),
        )
    }

    @Test
    fun `both languages expose the same example lists`() {
        val english = NotationTutorial.text(ChessMoveAnnouncer.Language.English)
        val french = NotationTutorial.text(ChessMoveAnnouncer.Language.French)
        assertEquals(english.writePawnAndPieceExamples.size, french.writePawnAndPieceExamples.size)
        assertEquals(english.readExamples.size, french.readExamples.size)
        assertEquals(english.speakTips.size, french.speakTips.size)
        assertEquals(
            english.writePawnAndPieceExamples.map { it.san },
            french.writePawnAndPieceExamples.map { it.san },
        )
    }

    private fun isLetterized(san: String, spoken: String): Boolean {
        val body = san.trim().trimEnd('#', '+')
        val letterized = body.replace("", " ").trim()
        return spoken.startsWith(letterized)
    }
}
