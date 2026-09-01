package com.blindfoldchess.trainer.core.chess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.MoveList

data class LoadedFamousGame(
    val game: FamousGame,
    val moves: List<ReplayMove>,
    val positions: List<List<OccupiedSquare>>,
)

class GameFollowDrill(
    private val catalog: List<FamousGame> = FamousGamesCatalog.games,
) {
    fun library(): List<FamousGame> = catalog

    fun load(id: String): LoadedFamousGame {
        val game = catalog.firstOrNull { it.id == id }
            ?: error("Unknown famous game: $id")
        return parseGame(game)
    }

    fun isExpected(move: ReplayMove, from: Square, to: Square): Boolean =
        move.from == from && move.to == to

    companion object {
        fun parseGame(game: FamousGame): LoadedFamousGame {
            val tokens = game.sanTokens
            val list = MoveList()
            try {
                list.loadFromSan(game.san)
            } catch (error: Exception) {
                throw IllegalArgumentException(
                    "Could not parse SAN for ${game.id}: ${error.message}",
                    error,
                )
            }
            require(list.size == tokens.size) {
                "SAN token count ${tokens.size} != parsed ${list.size} for ${game.id}"
            }

            val board = Board()
            val positions = mutableListOf(boardOccupancy(board))
            val moves = list.mapIndexed { index, move ->
                val replay = ReplayMove(
                    plyIndex = index,
                    moveNumber = index / 2 + 1,
                    isWhite = index % 2 == 0,
                    san = tokens[index],
                    from = move.toAppFrom(),
                    to = move.toAppTo(),
                )
                check(board.doMove(move)) {
                    "Illegal move ${tokens[index]} in ${game.id}"
                }
                positions += boardOccupancy(board)
                replay
            }
            return LoadedFamousGame(
                game = game,
                moves = moves,
                positions = positions,
            )
        }

        fun parseMoves(game: FamousGame): List<ReplayMove> = parseGame(game).moves
    }
}
