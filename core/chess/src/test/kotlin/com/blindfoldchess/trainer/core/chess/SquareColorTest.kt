package com.blindfoldchess.trainer.core.chess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SquareColorTest {

    @Test
    fun `a1 is dark`() {
        assertEquals(SquareColor.DARK, SquareColor.of(Square('a', 1)))
    }

    @Test
    fun `a2 is light`() {
        assertEquals(SquareColor.LIGHT, SquareColor.of(Square('a', 2)))
    }

    @Test
    fun `e4 is light`() {
        assertEquals(SquareColor.LIGHT, SquareColor.of(Square('e', 4)))
    }

    @Test
    fun `d5 is light`() {
        assertEquals(SquareColor.LIGHT, SquareColor.of(Square('d', 5)))
    }

    @Test
    fun `h8 is dark`() {
        assertEquals(SquareColor.DARK, SquareColor.of(Square('h', 8)))
    }

    @Test
    fun `drill validates answers`() {
        val drill = SquareColorDrill { Square('c', 3) }
        val question = drill.nextQuestion()

        assertEquals(SquareColor.DARK, question.correctColor)
        assertTrue(drill.isCorrect(question, SquareColor.DARK))
        assertFalse(drill.isCorrect(question, SquareColor.LIGHT))
    }
}