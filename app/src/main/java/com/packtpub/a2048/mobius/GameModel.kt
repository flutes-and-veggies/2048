package com.packtpub.a2048.mobius

import com.packtpub.a2048.viewmodel.CellData

data class GameModel(
    val score: Int = 0,
    val bestScore: Int = 0,
    val board: MutableMap<Pair<Int, Int>, CellData> = mutableMapOf(),
    val mergedPositions: List<Pair<Int, Int>> = listOf(),
    val newSpawnPositions: List<Pair<Int, Int>> = listOf()
)