package com.blindfoldchess.trainer.core.chess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.move.MoveList
import com.github.bhlangonijr.chesslib.Square as LibSquare

sealed class PlayResult {
    data class Played(val move: ReplayMove) : PlayResult()
    data class NeedsPromotion(val from: Square, val to: Square) : PlayResult()
    data class Ambiguous(
        val man: ChessMan,
        val to: Square,
        val origins: List<Square>,
    ) : PlayResult()
    data object Illegal : PlayResult()
}

data class ChessSnapshot(
    val pieces: List<OccupiedSquare>,
    val moves: List<ReplayMove>,
    val plyIndex: Int,
    val lineLength: Int,
    val isWhiteToMove: Boolean,
    val inCheck: Boolean,
    val isCheckmate: Boolean,
    val isStalemate: Boolean,
) {
    val canStepBack: Boolean get() = plyIndex > 0
    val canStepForward: Boolean get() = plyIndex < lineLength
    val atLatest: Boolean get() = plyIndex >= lineLength
}

class ChessSession(
    fen: String? = null,
) {
    private val startFen = fen ?: START_FEN
    private val board = Board()
    private val history = mutableListOf<ReplayMove>()
    private var ply = 0

    init {
        if (fen != null) {
            board.loadFromFen(fen)
        }
    }

    fun fen(): String = board.fen

    fun snapshot(): ChessSnapshot = ChessSnapshot(
        pieces = boardOccupancy(board),
        moves = history.take(ply),
        plyIndex = ply,
        lineLength = history.size,
        isWhiteToMove = board.sideToMove == Side.WHITE,
        inCheck = board.isKingAttacked,
        isCheckmate = board.isMated,
        isStalemate = board.isStaleMate,
    )

    fun playSan(san: String): PlayResult {
        val token = san.trim()
        if (token.isEmpty() || board.isMated || board.isStaleMate) return PlayResult.Illegal
        val fenBefore = board.fen
        return try {
            if (!board.doMove(token)) return PlayResult.Illegal
            val executed = board.undoMove() ?: return PlayResult.Illegal
            commit(fenBefore, executed, preferredSan = stripMoveNumber(token))
        } catch (_: Exception) {
            PlayResult.Illegal
        }
    }

    fun originsFor(man: ChessMan, to: Square, capture: Boolean = false): List<Square> {
        if (board.isMated || board.isStaleMate) return emptyList()
        val side = board.sideToMove
        return board.legalMoves().mapNotNull { move ->
            if (move.toAppTo() != to) return@mapNotNull null
            val piece = board.getPiece(move.getFrom())
            if (piece == Piece.NONE) return@mapNotNull null
            if (piece.pieceSide != side) return@mapNotNull null
            if (piece.pieceType.toChessMan() != man) return@mapNotNull null
            if (capture && !move.isCaptureOn(board)) return@mapNotNull null
            move.toAppFrom()
        }.distinct()
    }

    fun playPieceTo(
        man: ChessMan,
        to: Square,
        originFile: Char? = null,
        originRank: Int? = null,
        promotion: ChessMan? = ChessMan.QUEEN,
        capture: Boolean = false,
    ): PlayResult {
        val origins = originsFor(man, to, capture = capture).filter { origin ->
            (originFile == null || origin.file == originFile) &&
                (originRank == null || origin.rank == originRank)
        }
        return when (origins.size) {
            0 -> PlayResult.Illegal
            1 -> playSquares(origins.first(), to, promotion)
            else -> PlayResult.Ambiguous(man = man, to = to, origins = origins)
        }
    }

    fun playUci(uci: String): PlayResult {
        val token = uci.trim().lowercase()
        if (token.length < 4) return PlayResult.Illegal
        val from = Square.fromAlgebraic(token.substring(0, 2)) ?: return PlayResult.Illegal
        val to = Square.fromAlgebraic(token.substring(2, 4)) ?: return PlayResult.Illegal
        val promotion = when (token.getOrNull(4)) {
            'q' -> ChessMan.QUEEN
            'r' -> ChessMan.ROOK
            'b' -> ChessMan.BISHOP
            'n' -> ChessMan.KNIGHT
            else -> ChessMan.QUEEN
        }
        return playSquares(from, to, promotion)
    }

    fun playSquares(from: Square, to: Square, promotion: ChessMan? = null): PlayResult {
        if (board.isMated || board.isStaleMate) return PlayResult.Illegal
        val matches = board.legalMoves().filter { move ->
            move.toAppFrom() == from && move.toAppTo() == to
        }
        if (matches.isEmpty()) return PlayResult.Illegal
        val chosenPromotion = promotion ?: ChessMan.QUEEN
        val move = when {
            matches.size == 1 -> matches.first()
            matches.any { it.getPromotion() != Piece.NONE } ->
                matches.firstOrNull { candidate ->
                    candidate.getPromotion() != Piece.NONE &&
                        candidate.getPromotion().pieceType.toChessMan() == chosenPromotion
                } ?: return PlayResult.NeedsPromotion(from, to)
            else -> matches.first()
        }
        val fenBefore = board.fen
        return commit(fenBefore, move)
    }

    fun stepBack(): Boolean {
        if (ply == 0) return false
        board.undoMove() ?: return false
        ply--
        return true
    }

    fun stepForward(): Boolean {
        if (ply >= history.size) return false
        val next = history[ply]
        val matches = board.legalMoves().filter { move ->
            move.toAppFrom() == next.from && move.toAppTo() == next.to
        }
        if (matches.isEmpty()) return false
        if (!board.doMove(matches.first())) return false
        ply++
        return true
    }

    fun goToStart(): Boolean {
        if (ply == 0) return false
        while (stepBack()) Unit
        return true
    }

    fun goToLatest(): Boolean {
        if (ply >= history.size) return false
        while (stepForward()) Unit
        return true
    }

    fun undo(): Boolean {
        if (history.isEmpty()) return false
        goToLatest()
        board.undoMove() ?: return false
        history.removeAt(history.lastIndex)
        ply = history.size
        return true
    }

    fun reset() {
        board.loadFromFen(startFen)
        history.clear()
        ply = 0
    }

    private fun commit(
        fenBefore: String,
        executed: Move,
        preferredSan: String? = null,
    ): PlayResult.Played {
        val from = executed.toAppFrom()
        val to = executed.toAppTo()
        if (ply < history.size) {
            val next = history[ply]
            if (next.from == from && next.to == to) {
                check(board.doMove(executed)) { "Could not replay $from$to" }
                ply++
                return PlayResult.Played(next)
            }
            while (history.size > ply) {
                history.removeAt(history.lastIndex)
            }
        }
        check(board.doMove(executed)) { "Could not play $from$to" }
        val san = preferredSan?.takeIf { it.isNotBlank() } ?: sanFor(fenBefore, executed)
        val move = ReplayMove(
            plyIndex = ply,
            moveNumber = ply / 2 + 1,
            isWhite = ply % 2 == 0,
            san = san,
            from = from,
            to = to,
        )
        history += move
        ply++
        return PlayResult.Played(move)
    }

    private fun sanFor(fenBefore: String, move: Move): String {
        return try {
            val list = MoveList(fenBefore)
            list.add(move)
            list.toSanArray()?.lastOrNull()?.trim()?.ifBlank { null }
                ?: uci(move)
        } catch (_: Exception) {
            uci(move)
        }
    }

    private fun Move.isCaptureOn(board: Board): Boolean {
        val dest = board.getPiece(getTo())
        if (dest != Piece.NONE) return true
        val moving = board.getPiece(getFrom())
        if (moving.pieceType != LibPieceType.PAWN) return false
        val enPassant = board.enPassantTarget
        return enPassant != LibSquare.NONE && enPassant == getTo()
    }

    private fun uci(move: Move): String =
        move.toAppFrom().algebraic + move.toAppTo().algebraic

    private fun stripMoveNumber(san: String): String {
        val trimmed = san.trim()
        val afterDots = trimmed.substringAfterLast('.', missingDelimiterValue = trimmed).trim()
        return afterDots.ifBlank { trimmed }
    }

    companion object {
        const val START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }
}
