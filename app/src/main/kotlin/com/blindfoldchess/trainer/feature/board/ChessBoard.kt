package com.blindfoldchess.trainer.feature.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.core.chess.ChessMan
import com.blindfoldchess.trainer.core.chess.OccupiedSquare
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.core.chess.SquareColor
import com.blindfoldchess.trainer.ui.theme.Accent
import com.blindfoldchess.trainer.ui.theme.BoardArrow
import com.blindfoldchess.trainer.ui.theme.BoardDarkSquare
import com.blindfoldchess.trainer.ui.theme.BoardLightSquare
import com.blindfoldchess.trainer.ui.theme.BoardNotation
import com.blindfoldchess.trainer.ui.theme.BoardPieceBlack
import com.blindfoldchess.trainer.ui.theme.BoardPieceWhite
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect
import kotlin.math.atan2

data class SquareHighlight(
    val square: Square,
    val correct: Boolean,
)

data class BoardArrow(
    val from: Square,
    val to: Square,
)

fun arrowsAlong(start: Square?, steps: List<Square>): List<BoardArrow> {
    if (start == null || steps.isEmpty()) return emptyList()
    return (listOf(start) + steps).zipWithNext(::BoardArrow)
}

fun visitedSquares(arrows: List<BoardArrow>): List<Square> {
    if (arrows.isEmpty()) return emptyList()
    return buildList {
        add(arrows.first().from)
        arrows.forEach { add(it.to) }
    }.distinct()
}

private val FILES = ('a'..'h').toList()
private val RANKS = (8 downTo 1).toList()
private val NotationSize = 20.dp
private const val PieceFontFactor = 0.80f
private const val ArrowAlpha = 0.70f
private const val ArrivalCircleDiameterFactor = 0.90f

fun squareFromGrid(col: Int, row: Int, flipped: Boolean = false): Square? {
    if (col !in 0..7 || row !in 0..7) return null
    val file = if (flipped) ('h' - col) else ('a' + col)
    val rank = if (flipped) row + 1 else 8 - row
    return Square(file, rank)
}

private fun files(flipped: Boolean): List<Char> = if (flipped) FILES.asReversed() else FILES

private fun ranks(flipped: Boolean): List<Int> = if (flipped) RANKS.asReversed() else RANKS

private fun gridCol(file: Char, flipped: Boolean): Int =
    if (flipped) 'h' - file else file - 'a'

private fun gridRow(rank: Int, flipped: Boolean): Int =
    if (flipped) rank - 1 else 8 - rank

@Composable
fun ChessBoard(
    showNotation: Boolean,
    modifier: Modifier = Modifier,
    highlight: SquareHighlight? = null,
    showArrows: Boolean = false,
    arrows: List<BoardArrow> = emptyList(),
    showPieces: Boolean = true,
    pieces: List<OccupiedSquare> = emptyList(),
    selectedSquare: Square? = null,
    onSquareClick: ((Square) -> Unit)? = null,
    flipped: Boolean = false,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Espace toujours réservé pour que l'échiquier ne bouge pas quand on toggle les coords.
        val availableHeight = if (maxHeight.value.isFinite()) maxHeight else maxWidth
        val side = minOf(maxWidth - NotationSize, availableHeight - NotationSize).coerceAtLeast(0.dp)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(NotationSize)
                        .height(side),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showNotation) {
                        RankNotationColumn(boardHeight = side, flipped = flipped)
                    }
                }

                BoardCanvas(
                    highlight = highlight,
                    arrows = if (showArrows) arrows else emptyList(),
                    pieces = if (showPieces) pieces else emptyList(),
                    selectedSquare = selectedSquare,
                    onSquareClick = onSquareClick,
                    flipped = flipped,
                    modifier = Modifier
                        .width(side)
                        .height(side),
                )
            }

            Box(
                modifier = Modifier
                    .width(side + NotationSize)
                    .height(NotationSize),
                contentAlignment = Alignment.Center,
            ) {
                if (showNotation) {
                    FileNotationRow(
                        boardWidth = side,
                        flipped = flipped,
                        modifier = Modifier.width(side + NotationSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardCanvas(
    highlight: SquareHighlight?,
    arrows: List<BoardArrow>,
    pieces: List<OccupiedSquare>,
    selectedSquare: Square?,
    onSquareClick: ((Square) -> Unit)?,
    flipped: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val squareSize = size.width / 8f
            for (rowIndex in 0..7) {
                for (colIndex in 0..7) {
                    val square = squareFromGrid(colIndex, rowIndex, flipped) ?: continue
                    val squareColor = SquareColor.of(square)
                    val color = if (squareColor == SquareColor.LIGHT) BoardLightSquare else BoardDarkSquare
                    drawRect(
                        color = color,
                        topLeft = Offset(colIndex * squareSize, rowIndex * squareSize),
                        size = Size(squareSize, squareSize),
                    )
                }
            }
            selectedSquare?.let { selected ->
                val colIndex = gridCol(selected.file, flipped)
                val rowIndex = gridRow(selected.rank, flipped)
                val topLeft = Offset(colIndex * squareSize, rowIndex * squareSize)
                drawRect(
                    color = Accent.copy(alpha = 0.42f),
                    topLeft = topLeft,
                    size = Size(squareSize, squareSize),
                )
                val strokeWidth = 3.dp.toPx()
                val inset = strokeWidth / 2f
                drawRect(
                    color = Accent,
                    topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                    size = Size(squareSize - strokeWidth, squareSize - strokeWidth),
                    style = Stroke(width = strokeWidth),
                )
            }
        }

        BoardPieces(pieces = pieces, flipped = flipped)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val squareSize = size.width / 8f
            val arrowColor = BoardArrow.copy(alpha = ArrowAlpha)
            val markerRadius = squareSize * 0.18f
            arrows.forEach { arrow ->
                drawMoveArrow(
                    from = squareCenter(arrow.from, squareSize, flipped),
                    to = squareCenter(arrow.to, squareSize, flipped),
                    squareSize = squareSize,
                    markerRadius = markerRadius,
                    color = arrowColor,
                )
            }
            arrows.map { it.to }.distinct().forEach { square ->
                drawCircle(
                    color = arrowColor,
                    radius = squareSize * ArrivalCircleDiameterFactor / 2f,
                    center = squareCenter(square, squareSize, flipped),
                    style = Stroke(width = 3.dp.toPx()),
                )
            }

            highlight?.let {
                val colIndex = gridCol(it.square.file, flipped)
                val rowIndex = gridRow(it.square.rank, flipped)
                val accent = if (it.correct) Correct else Incorrect
                val topLeft = Offset(colIndex * squareSize, rowIndex * squareSize)
                val square = Size(squareSize, squareSize)
                drawRect(
                    color = accent.copy(alpha = 0.72f),
                    topLeft = topLeft,
                    size = square,
                )
                val strokeWidth = 4.dp.toPx()
                val inset = strokeWidth / 2f
                drawRect(
                    color = accent,
                    topLeft = Offset(topLeft.x + inset, topLeft.y + inset),
                    size = Size(squareSize - strokeWidth, squareSize - strokeWidth),
                    style = Stroke(width = strokeWidth),
                )
            }
        }

        if (onSquareClick != null) {
            BoardTapOverlay(onSquareClick = onSquareClick, flipped = flipped)
        }
    }
}

@Composable
private fun BoardTapOverlay(
    onSquareClick: (Square) -> Unit,
    flipped: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ranks(flipped).forEach { rank ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                files(flipped).forEach { file ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSquareClick(Square(file, rank)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardPieces(
    pieces: List<OccupiedSquare>,
    flipped: Boolean,
) {
    if (pieces.isEmpty()) return
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val squareDp = maxWidth / 8
        pieces.forEach { piece ->
            val col = gridCol(piece.square.file, flipped)
            val row = gridRow(piece.square.rank, flipped)
            Box(
                modifier = Modifier
                    .offset(x = squareDp * col, y = squareDp * row)
                    .size(squareDp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = pieceGlyph(piece.man),
                    color = if (piece.isWhite) BoardPieceWhite else BoardPieceBlack,
                    fontSize = (squareDp.value * PieceFontFactor).sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = if (piece.isWhite) Color(0xFF111111) else Color(0xDDEDE6D6),
                            offset = Offset.Zero,
                            blurRadius = if (piece.isWhite) 5f else 8f,
                        ),
                    ),
                )
            }
        }
    }
}

private fun pieceGlyph(man: ChessMan): String {
    val glyph = when (man) {
        ChessMan.KING -> "\u265A"
        ChessMan.QUEEN -> "\u265B"
        ChessMan.ROOK -> "\u265C"
        ChessMan.BISHOP -> "\u265D"
        ChessMan.KNIGHT -> "\u265E"
        ChessMan.PAWN -> "\u265F"
    }
    return glyph + "\uFE0E"
}

private fun squareCenter(square: Square, squareSize: Float, flipped: Boolean): Offset {
    val colIndex = gridCol(square.file, flipped)
    val rowIndex = gridRow(square.rank, flipped)
    return Offset(
        x = colIndex * squareSize + squareSize / 2f,
        y = rowIndex * squareSize + squareSize / 2f,
    )
}

private fun DrawScope.drawMoveArrow(
    from: Offset,
    to: Offset,
    squareSize: Float,
    markerRadius: Float,
    color: Color,
) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val length = kotlin.math.hypot(dx, dy)
    if (length < 1f) return

    val dir = Offset(dx / length, dy / length)
    val normal = Offset(-dir.y, dir.x)
    val start = from + dir * markerRadius
    val tip = to - dir * markerRadius
    val shaft = (squareSize * 0.30f).coerceIn(8f, 18f)
    val half = shaft / 2f
    val available = kotlin.math.hypot(tip.x - start.x, tip.y - start.y)
    val headLength = (shaft * 4.1f)
        .coerceAtMost(available * 0.75f)
        .coerceAtLeast(1f)
    val headHalf = half * 4.2f
    val base = tip - dir * headLength

    val tailLeft = start + normal * half
    val tailRight = start - normal * half
    val path = Path().apply {
        moveTo(tailLeft.x, tailLeft.y)
        lineTo((base + normal * half).x, (base + normal * half).y)
        lineTo((base + normal * headHalf).x, (base + normal * headHalf).y)
        lineTo(tip.x, tip.y)
        lineTo((base - normal * headHalf).x, (base - normal * headHalf).y)
        lineTo((base - normal * half).x, (base - normal * half).y)
        lineTo(tailRight.x, tailRight.y)
        arcTo(
            rect = Rect(
                left = start.x - half,
                top = start.y - half,
                right = start.x + half,
                bottom = start.y + half,
            ),
            startAngleDegrees = Math.toDegrees(
                atan2(tailRight.y - start.y, tailRight.x - start.x).toDouble(),
            ).toFloat(),
            sweepAngleDegrees = -180f,
            forceMoveTo = false,
        )
        close()
    }
    drawPath(path = path, color = color)
}

@Composable
private fun FileNotationRow(
    boardWidth: Dp,
    flipped: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(boardWidth),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(NotationSize))
        files(flipped).forEach { file ->
            Text(
                text = file.toString(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = BoardNotation,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun RankNotationColumn(
    boardHeight: Dp,
    flipped: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.height(boardHeight),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ranks(flipped).forEach { rank ->
            Text(
                text = rank.toString(),
                color = BoardNotation,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}