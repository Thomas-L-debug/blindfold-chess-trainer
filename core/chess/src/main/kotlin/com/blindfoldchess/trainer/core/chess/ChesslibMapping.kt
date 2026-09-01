package com.blindfoldchess.trainer.core.chess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType as LibPieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import com.github.bhlangonijr.chesslib.Square as LibSquare

internal fun boardOccupancy(board: Board): List<OccupiedSquare> = buildList {
    for (file in 'a'..'h') {
        for (rank in 1..8) {
            val libSquare = Square(file, rank).toLibSquare()
            val piece = board.getPiece(libSquare)
            if (piece == Piece.NONE) continue
            add(
                OccupiedSquare(
                    square = Square(file, rank),
                    man = piece.pieceType.toChessMan(),
                    isWhite = piece.pieceSide == Side.WHITE,
                ),
            )
        }
    }
}

internal fun Square.toLibSquare(): LibSquare =
    LibSquare.valueOf("${file.uppercaseChar()}$rank")

internal fun LibSquare.toAppSquare(): Square {
    val algebraic = name.lowercase()
    return Square.fromAlgebraic(algebraic)
        ?: error("Unmappable chesslib square $this")
}

internal fun Move.toAppFrom(): Square = getFrom().toAppSquare()

internal fun Move.toAppTo(): Square = getTo().toAppSquare()

internal fun LibPieceType.toChessMan(): ChessMan = when (this) {
    LibPieceType.KING -> ChessMan.KING
    LibPieceType.QUEEN -> ChessMan.QUEEN
    LibPieceType.ROOK -> ChessMan.ROOK
    LibPieceType.BISHOP -> ChessMan.BISHOP
    LibPieceType.KNIGHT -> ChessMan.KNIGHT
    LibPieceType.PAWN -> ChessMan.PAWN
    else -> error("Unexpected piece type $this")
}

internal fun ChessMan.toPromotionPiece(isWhite: Boolean): Piece {
    val type = when (this) {
        ChessMan.QUEEN -> LibPieceType.QUEEN
        ChessMan.ROOK -> LibPieceType.ROOK
        ChessMan.BISHOP -> LibPieceType.BISHOP
        ChessMan.KNIGHT -> LibPieceType.KNIGHT
        ChessMan.KING, ChessMan.PAWN -> error("Invalid promotion $this")
    }
    return Piece.make(if (isWhite) Side.WHITE else Side.BLACK, type)
}
