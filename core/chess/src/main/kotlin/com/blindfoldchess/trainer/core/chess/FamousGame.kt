package com.blindfoldchess.trainer.core.chess

data class FamousGame(
    val id: String,
    val title: String,
    val white: String,
    val black: String,
    val event: String,
    val year: Int,
    val result: String,
    val san: String,
) {
    val sanTokens: List<String>
        get() = san.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    val plyCount: Int get() = sanTokens.size

    val fullMoveCount: Int get() = (plyCount + 1) / 2
}

data class ReplayMove(
    val plyIndex: Int,
    val moveNumber: Int,
    val isWhite: Boolean,
    val san: String,
    val from: Square,
    val to: Square,
) {
    val prompt: String
        get() = if (isWhite) "$moveNumber. $san" else "$moveNumber... $san"
}

fun formatPlayedMoves(moves: List<ReplayMove>, plyIndex: Int): String {
    if (plyIndex <= 0) return ""
    return buildString {
        moves.take(plyIndex).forEach { move ->
            if (move.isWhite) {
                if (isNotEmpty()) append("  ")
                append(move.moveNumber).append(". ").append(move.san)
            } else {
                append(' ').append(move.san)
            }
        }
    }
}
