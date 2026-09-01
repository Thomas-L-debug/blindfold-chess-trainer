package com.blindfoldchess.trainer.core.chess

enum class ChessMan {
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN,
}

data class OccupiedSquare(
    val square: Square,
    val man: ChessMan,
    val isWhite: Boolean,
)
