package com.blindfoldchess.trainer.core.chess

import kotlin.math.abs

enum class PieceType {
    BISHOP,
    KNIGHT,
    ROOK,
    QUEEN,
    ;

    /**
     * Legal move on an empty board (no blocking pieces).
     * Same-square "moves" are never legal.
     */
    fun canMove(from: Square, to: Square): Boolean {
        if (from == to) return false
        val fileDelta = abs(from.file - to.file)
        val rankDelta = abs(from.rank - to.rank)
        return when (this) {
            BISHOP -> fileDelta == rankDelta
            KNIGHT -> (fileDelta == 1 && rankDelta == 2) || (fileDelta == 2 && rankDelta == 1)
            ROOK -> fileDelta == 0 || rankDelta == 0
            QUEEN -> fileDelta == rankDelta || fileDelta == 0 || rankDelta == 0
        }
    }
}
