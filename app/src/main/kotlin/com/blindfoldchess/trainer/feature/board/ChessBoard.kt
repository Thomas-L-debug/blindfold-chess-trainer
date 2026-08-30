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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blindfoldchess.trainer.core.chess.Square
import com.blindfoldchess.trainer.core.chess.SquareColor
import com.blindfoldchess.trainer.ui.theme.BoardDarkSquare
import com.blindfoldchess.trainer.ui.theme.BoardLightSquare
import com.blindfoldchess.trainer.ui.theme.BoardNotation

private val FILES = ('a'..'h').toList()
private val RANKS = (8 downTo 1).toList()
private val NotationSize = 20.dp

@Composable
fun ChessBoard(
    showNotation: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        // Espace toujours réservé pour que l'échiquier ne bouge pas quand on toggle les coords.
        val side = minOf(maxWidth - NotationSize, maxHeight - NotationSize)

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
private fun BoardCanvas(modifier: Modifier = Modifier) {
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
    }
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