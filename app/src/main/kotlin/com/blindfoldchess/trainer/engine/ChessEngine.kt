package com.blindfoldchess.trainer.engine

interface ChessEngine {
    suspend fun bestMove(fen: String, elo: Int): String?
    fun close()
}

fun parseBestMove(line: String): String? {
    val tokens = line.trim().split(Regex("\\s+"))
    if (tokens.size < 2 || tokens[0] != "bestmove") return null
    val move = tokens[1]
    if (move.isEmpty() || move == "(none)" || move == "0000") return null
    return move
}

fun clampStockfishElo(elo: Int): Int = elo.coerceIn(MIN_ELO, MAX_ELO)

fun movetimeMsForElo(elo: Int): Long {
    val clamped = clampStockfishElo(elo)
    val t = (clamped - MIN_ELO).toDouble() / (MAX_ELO - MIN_ELO).toDouble()
    return (150 + t * 850).toLong()
}

const val MIN_ELO = 1350
const val MAX_ELO = 2850

val BOT_ELO_LEVELS = listOf(1350, 1500, 1700, 1900, 2100, 2300, 2500)
