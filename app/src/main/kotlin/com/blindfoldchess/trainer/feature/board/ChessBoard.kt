package com.blindfoldchess.trainer.feature.board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.atan2
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.core.chess.SquareColor
import com.blindfoldchess.trainer.ui.theme.BoardArrow
import com.blindfoldchess.trainer.ui.theme.BoardDarkSquare
import com.blindfoldchess.trainer.ui.theme.BoardLightSquare
import com.blindfoldchess.trainer.ui.theme.BoardNotation
import com.blindfoldchess.trainer.ui.theme.Correct
import com.blindfoldchess.trainer.ui.theme.Incorrect

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

@Composable
fun ChessBoard(
    showNotation: Boolean,
    modifier: Modifier = Modifier,
    highlight: SquareHighlight? = null,
    showArrows: Boolean = false,
    arrows: List<BoardArrow> = emptyList(),
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
                        RankNotationColumn(boardHeight = side)
                    }
                }

                BoardCanvas(
                    highlight = highlight,
                    arrows = if (showArrows) arrows else emptyList(),
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
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val squareSize = size.width / 8f
        RANKS.forEachIndexed { rowIndex, rank ->
            FILES.forEachIndexed { colIndex, file ->
                val squareColor = SquareColor.of(Square(file, rank))
                val color = if (squareColor == SquareColor.LIGHT) BoardLightSquare else BoardDarkSquare
                drawRect(
                    color = color,
                    topLeft = Offset(colIndex * squareSize, rowIndex * squareSize),
                    size = Size(squareSize, squareSize),
                )
            }
        }

        val markerRadius = squareSize * 0.18f
        arrows.forEach { arrow ->
            drawMoveArrow(
                from = squareCenter(arrow.from, squareSize),
                to = squareCenter(arrow.to, squareSize),
                squareSize = squareSize,
                markerRadius = markerRadius,
            )
        }
        visitedSquares(arrows).forEach { square ->
            drawCircle(
                color = BoardArrow,
                radius = markerRadius,
                center = squareCenter(square, squareSize),
            )
        }

        highlight?.let {
            val colIndex = it.square.file - 'a'
            val rowIndex = 8 - it.square.rank
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
}

private fun squareCenter(square: Square, squareSize: Float): Offset {
    val colIndex = square.file - 'a'
    val rowIndex = 8 - square.rank
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
    drawPath(path = path, color = BoardArrow)
}

@Composable
private fun FileNotationRow(
    boardWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.width(boardWidth),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(NotationSize))
        FILES.forEach { file ->
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.height(boardHeight),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RANKS.forEach { rank ->
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