package com.blindfoldchess.trainer.core.chess

data class PiecePathPuzzle(
    val piece: PieceType,
    val start: Square,
    val target: Square,
)

class PiecePathDrill(
    private val squareProvider: () -> Square = { Square.random() },
) {
    fun nextPuzzle(piece: PieceType): PiecePathPuzzle {
        val start = squareProvider()
        val target = generateSequence { squareProvider() }
            .take(128)
            .firstOrNull { isValidTarget(piece, start, it) }
            ?: firstValidTarget(piece, start)
        return PiecePathPuzzle(piece = piece, start = start, target = target)
    }

    fun isLegalMove(piece: PieceType, from: Square, to: Square): Boolean =
        piece.canMove(from, to)

    private fun isValidTarget(piece: PieceType, start: Square, target: Square): Boolean {
        if (target == start) return false
        if (piece == PieceType.BISHOP && SquareColor.of(target) != SquareColor.of(start)) {
            return false
        }
        return true
    }

    private fun firstValidTarget(piece: PieceType, start: Square): Square {
        for (file in 'a'..'h') {
            for (rank in 1..8) {
                val candidate = Square(file, rank)
                if (isValidTarget(piece, start, candidate)) return candidate
            }
        }
        error("No valid target from $start for $piece")
    }
}
