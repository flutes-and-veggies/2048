package com.packtpub.a2048.composable

import androidx.compose.runtime.Composable
import com.packtpub.a2048.viewmodel.GameViewModel

@Composable
fun Board(viewModel: GameViewModel) {
    SquareGrid(viewModel = viewModel)
}