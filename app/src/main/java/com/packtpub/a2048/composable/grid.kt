package com.packtpub.a2048.composable

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.packtpub.a2048.extensions.toBackgroundAndFontColor
import com.packtpub.a2048.ui.theme.BoardColors
import com.packtpub.a2048.ui.theme.LocalBoardColors
import com.packtpub.a2048.viewmodel.Board
import com.packtpub.a2048.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CellAnimationData(
    val offset: Animatable<Offset, AnimationVector2D>,
    val size: Animatable<Float, AnimationVector1D>,
    val value: Int
)

private fun calcOffset(
    squareSizePx: Float,
    squareSpacingPx: Float,
    colIndex: Int,
    rowIndex: Int
): Offset {
    val rectXOffset = 2 * squareSpacingPx + colIndex * (squareSizePx + squareSpacingPx)
    val rectYOffset = 2 * squareSpacingPx + rowIndex * (squareSizePx + squareSpacingPx)
    return Offset(rectXOffset, rectYOffset)
}

@Composable
fun SquareGrid(
    viewModel: GameViewModel
) {
    val numCols = viewModel.numCols
    val numRows = viewModel.numRows

    val board = viewModel.boardFlow.collectAsState(mapOf()).value
    val mergedPositions = viewModel.mergedPositions.collectAsState().value
    val newSpawnPositions = viewModel.newSpawnPositions.collectAsState().value

    val boardColors = LocalBoardColors.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val squareSpacing = 12.dp
    val fontSize = 88.sp
    val animationTime = 200

    val squareSize = (screenWidth - (numCols + 3).times(squareSpacing)) / numCols
    val squareSizePx = with(LocalDensity.current) { squareSize.toPx() }
    val squareSpacingPx = with(LocalDensity.current) { squareSpacing.toPx() }
    val cornerRadius = CornerRadius(squareSpacingPx / 5)

    val canvasWidth = (numCols * squareSizePx + (numCols - 1) * squareSpacingPx)
    val canvasHeight = (numRows * squareSizePx + (numRows - 1) * squareSpacingPx)

    val backgroundWidth = canvasWidth + 2 * squareSpacingPx
    val backgroundHeight = canvasHeight + 2 * squareSpacingPx

    val verticalCenteringOffset = (screenHeight + 2 * squareSpacing - backgroundHeight.dp / 2) / 2

    var previousBoard = remember { mutableStateOf<Board?>(null) }
    val animatedBoard = remember { mutableStateMapOf<Pair<Int, Int>, CellAnimationData>() }

    // Changing the board
    LaunchedEffect(board, newSpawnPositions) {
        delay(animationTime.toLong())
        repeat(numCols) { colIndex ->
            repeat(numRows) { rowIndex ->
                val newPosition = Pair(rowIndex, colIndex)
                val hasJustAppeared = newSpawnPositions.contains(newPosition)
                val hasMerged = mergedPositions.contains(newPosition)
                val finalOffset = calcOffset(squareSizePx, squareSpacingPx, colIndex, rowIndex)

                val animOffset = Animatable(finalOffset, Offset.VectorConverter)
                val animSize = Animatable(if (hasJustAppeared) 0f else squareSizePx, Float.VectorConverter)

                val cell = CellAnimationData(
                    offset = animOffset,
                    size = animSize,
                    value = board[newPosition]?.value ?: 0
                )

                animatedBoard[newPosition] = cell

                if (hasJustAppeared) {
                    launch {
                        animSize.animateTo(
                            targetValue = squareSizePx,
                            animationSpec = tween(durationMillis = animationTime)
                        )
                    }
                } else if (hasMerged) {
                    launch {
                        animSize.animateTo(
                            targetValue = squareSizePx + 20,
                            animationSpec = tween(durationMillis = 50)
                        )
                        animSize.animateTo(
                            targetValue = squareSizePx - 20,
                            animationSpec = tween(durationMillis = 50)
                        )
                        animSize.animateTo(
                            targetValue = squareSizePx,
                            animationSpec = tween(durationMillis = 50)
                        )
                    }
                }
            }
        }

        previousBoard.value = board
    }

    // Animation effect
    LaunchedEffect(board, newSpawnPositions) {
        val prevBoard = previousBoard.value
        if (prevBoard == null) { return@LaunchedEffect }

        repeat(numCols) { colIndex ->
            repeat(numRows) { rowIndex ->
                val newPosition = Pair(rowIndex, colIndex)
                val initialPosition = prevBoard.filter {
                    it.value.id == board[newPosition]?.id
                }.keys.firstOrNull()
                if (initialPosition == null || prevBoard[initialPosition]?.value == 0) { return@repeat }

                // Animate initial position to the offset in new board
                launch {
                    delay(100)
                    animatedBoard[initialPosition]?.offset?.animateTo(
                        targetValue = calcOffset(squareSizePx, squareSpacingPx, colIndex, rowIndex),
                        animationSpec = tween(durationMillis = animationTime)
                    )
                }
            }
        }
    }

    Canvas(
        modifier = Modifier
            .width(backgroundWidth.dp)
            .height(backgroundHeight.dp)
            .padding(top = verticalCenteringOffset)
    ) {
        drawRoundRect(
            topLeft = Offset(
                squareSpacingPx, squareSpacingPx
            ),
            cornerRadius = cornerRadius,
            color = boardColors.boardBackground,
            size = Size(backgroundWidth, backgroundHeight)
        )
        drawBoardBackground(
            numCols = numCols,
            numRows = numRows,
            cornerRadius = cornerRadius,
            squareSizePx = squareSizePx,
            squareSpacingPx = squareSpacingPx,
            fontSize = fontSize,
            boardColors = boardColors
        )
        repeat(numCols) { colIndex ->
            repeat(numRows) { rowIndex ->
                val cell = animatedBoard[Pair(rowIndex, colIndex)]
                if (cell != null && cell.value != 0) {
                    drawRectangleWithNumber(
                        boardColors = boardColors,
                        offset = cell.offset.value,
                        number = cell.value,
                        cornerRadius = cornerRadius,
                        squareSizePx = cell.size.value,
                        maxSquareSizePx = squareSizePx,
                        fontSize = fontSize
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBoardBackground(
    numCols: Int,
    numRows: Int,
    cornerRadius: CornerRadius,
    squareSizePx: Float,
    squareSpacingPx: Float,
    fontSize: TextUnit,
    boardColors: BoardColors
) {
    repeat(numCols) { colIndex ->
        repeat(numRows) { rowIndex ->
            val offset = calcOffset(squareSizePx, squareSpacingPx, colIndex, rowIndex)
            drawRectangleWithNumber(
                boardColors = boardColors,
                offset = offset,
                number = 0,
                cornerRadius = cornerRadius,
                squareSizePx = squareSizePx,
                maxSquareSizePx = squareSizePx,
                fontSize = fontSize
            )
        }
    }
}

fun DrawScope.drawRectangleWithNumber(
    boardColors: BoardColors,
    offset: Offset,
    cornerRadius: CornerRadius,
    squareSizePx: Float,
    maxSquareSizePx: Float,
    fontSize: TextUnit,
    number: Int?
) {
    var backgroundColor = boardColors.emptyCellBackground
    var fontColor = boardColors.emptyCellBackground

    number?.let {
        val (newBgColor, newFontColor) = it.toBackgroundAndFontColor(boardColors)
        backgroundColor = newBgColor
        fontColor = newFontColor
    }

    val numberPaint = Paint().apply {
        color = Color.argb(
            (fontColor.alpha * 255).toInt(),
            (fontColor.red * 255).toInt(),
            (fontColor.green * 255).toInt(),
            (fontColor.blue * 255).toInt()
        )
        isFakeBoldText = true
        textSize = (squareSizePx * fontSize.value) / maxSquareSizePx
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    drawRoundRect(
        topLeft = Offset(
            offset.x + ((maxSquareSizePx - squareSizePx) / 2),
            offset.y + (maxSquareSizePx - squareSizePx) / 2
        ),
        cornerRadius = cornerRadius,
        color = backgroundColor,
        size = Size(squareSizePx, squareSizePx)
    )

    if (number != null) {
        drawContext.canvas.nativeCanvas.drawText(
            number.toString(),
            offset.x + ((maxSquareSizePx - squareSizePx) / 2) + squareSizePx / 2,
            offset.y + fontSize.value / 3 + ((maxSquareSizePx - squareSizePx) / 2) + squareSizePx / 2,
            numberPaint
        )
    }
}

@Preview
@Composable
fun PreviewSquare() {
    SquareGrid(GameViewModel(4, 4))
}