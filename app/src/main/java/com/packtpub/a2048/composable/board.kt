package com.packtpub.a2048.composable

import androidx.compose.runtime.Composable
import com.packtpub.a2048.mobius.GameModel

@Composable
fun Board(model: GameModel) {
    SquareGrid(model = model)
}