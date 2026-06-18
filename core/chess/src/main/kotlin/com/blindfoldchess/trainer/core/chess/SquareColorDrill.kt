package com.blindfoldchess.trainer.core.chess

data class SquareColorQuestion(
    val square: Square,
    val correctColor: SquareColor,
)

class SquareColorDrill(
    private val squareProvider: () -> Square = { Square.random() },
) {
    fun nextQuestion(): SquareColorQuestion {
        val square = squareProvider()
        return SquareColorQuestion(
            square = square,
            correctColor = SquareColor.of(square),
        )
    }

    fun isCorrect(
        question: SquareColorQuestion,
        answer: SquareColor,
    ): Boolean = question.correctColor == answer
}