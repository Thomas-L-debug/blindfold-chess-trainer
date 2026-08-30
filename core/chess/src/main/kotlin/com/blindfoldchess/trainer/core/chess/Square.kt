package com.blindfoldchess.trainer.core.chess

data class Square(
    val file: Char,
    val rank: Int,
) {
    init {
        require(file in 'a'..'h') { "File must be between a and h, got $file" }
        require(rank in 1..8) { "Rank must be between 1 and 8, got $rank" }
    }

    val algebraic: String get() = "$file$rank"

    companion object {
        fun fromAlgebraic(notation: String): Square? {
            if (notation.length != 2) return null
            val file = notation[0].lowercaseChar()
            val rank = notation[1].digitToIntOrNull() ?: return null
            return runCatching { Square(file, rank) }.getOrNull()
        }

        fun random(): Square {
            val file = ('a'..'h').random()
            val rank = (1..8).random()
            return Square(file, rank)
        }
    }
}