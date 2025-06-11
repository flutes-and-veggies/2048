package com.packtpub.a2048.extensions

import androidx.compose.ui.graphics.Color
import com.packtpub.a2048.ui.theme.BoardColors

fun Int.toBackgroundAndFontColor(
    boardColors: BoardColors
): Pair<Color, Color> = when (this) {
    0 -> Pair(boardColors.emptyCellBackground, boardColors.emptyCellBackground)
    2 -> Pair(boardColors.tile2Background, toFontColor(boardColors))
    4 -> Pair(boardColors.tile4Background, toFontColor(boardColors))
    8 -> Pair(boardColors.tile8Background, toFontColor(boardColors))
    16 -> Pair(boardColors.tile16Background, toFontColor(boardColors))
    32 -> Pair(boardColors.tile32Background, toFontColor(boardColors))
    64 -> Pair(boardColors.tile64Background, toFontColor(boardColors))
    128 -> Pair(boardColors.tile128Background, toFontColor(boardColors))
    256 -> Pair(boardColors.tile256Background, toFontColor(boardColors))
    512 -> Pair(boardColors.tile512Background, toFontColor(boardColors))
    1024 -> Pair(boardColors.tile1024Background, toFontColor(boardColors))
    2048 -> Pair(boardColors.tile2048Background, toFontColor(boardColors))
    else -> Pair(boardColors.tileSuperBackground, toFontColor(boardColors)) // for >2048
}


private fun Int.toFontColor(boardColors: BoardColors): Color = when (this) {
    2 -> boardColors.tileLowText
    4 -> boardColors.tileLowText
    else -> boardColors.tileHighText
}