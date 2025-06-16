package com.packtpub.a2048.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class BoardColors(
    val boardBackground: Color = Color(0xFFB9ADA2),
    val emptyCellBackground: Color = Color(0xFFCBC0B5),

    // Background colors for tile values
    val tile2Background: Color = Color(0xFFEEE4DA),
    val tile4Background: Color = Color(0xFFEDE0C8),
    val tile8Background: Color = Color(0xFFF2B179),
    val tile16Background: Color = Color(0xFFF59563),
    val tile32Background: Color = Color(0xFFF67C5F),
    val tile64Background: Color = Color(0xFFF65E3B),
    val tile128Background: Color = Color(0xFFEDCF72),
    val tile256Background: Color = Color(0xFFEDCC61),
    val tile512Background: Color = Color(0xFFEDC850),
    val tile1024Background: Color = Color(0xFFEDC53F),
    val tile2048Background: Color = Color(0xFFEDC22E),
    val tileSuperBackground: Color = Color(0xFF3C3A32),

    // Text colors for tile values
    val tileLowText: Color = Color(0xFF776E65), // for 2, 4
    val tileHighText: Color = Color(0xFFF9F6F2), // for >= 8

    // Button colors
    val primaryButtonBackground: Color = Color(0xFFB9ADA1),
    val secondaryButtonBackground: Color = Color(0xFF8C7B68)
)

val LocalBoardColors = staticCompositionLocalOf {
    BoardColors()
}
