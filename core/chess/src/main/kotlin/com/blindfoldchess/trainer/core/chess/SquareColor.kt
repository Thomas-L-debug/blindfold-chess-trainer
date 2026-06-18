package com.blindfoldchess.trainer.core.chess

enum class SquareColor {
    LIGHT,
    DARK,
    ;

    companion object {
        fun of(square: Square): SquareColor {
            val fileIndex = square.file - 'a'
            val rankIndex = square.rank - 1
            return if ((fileIndex + rankIndex) % 2 == 0) DARK else LIGHT
        }
    }
}