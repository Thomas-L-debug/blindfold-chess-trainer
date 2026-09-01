package com.blindfoldchess.trainer.core.chess

import java.util.ArrayDeque
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FindSquareDrillTest {

    @Test
    fun `next question uses the provided square`() {
        val drill = FindSquareDrill { Square('e', 4) }
        assertEquals(Square('e', 4), drill.nextQuestion())
    }

    @Test
    fun `avoids repeating the previous square when possible`() {
        val squares = ArrayDeque(listOf(Square('a', 1), Square('h', 8)))
        val drill = FindSquareDrill { squares.removeFirst() }
        val first = drill.nextQuestion()
        val second = drill.nextQuestion(avoid = first)
        assertEquals(Square('a', 1), first)
        assertEquals(Square('h', 8), second)
    }

    @Test
    fun `isCorrect compares the tapped square to the target`() {
        val drill = FindSquareDrill()
        val target = Square('c', 6)
        assertTrue(drill.isCorrect(target, Square('c', 6)))
        assertFalse(drill.isCorrect(target, Square('c', 5)))
    }
}
