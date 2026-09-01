package com.blindfoldchess.trainer.core.chess

class FindSquareDrill(
    private val squareProvider: () -> Square = { Square.random() },
) {
    fun nextQuestion(avoid: Square? = null): Square {
        var square = squareProvider()
        if (avoid != null) {
            var attempts = 0
            while (square == avoid && attempts < 32) {
                square = squareProvider()
                attempts++
            }
        }
        return square
    }

    fun isCorrect(target: Square, answer: Square): Boolean = target == answer
}
