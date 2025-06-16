package com.packtpub.a2048.mobius

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import java.util.UUID

data class AddedScoreData(
    val value: Int,
    val opacity: Animatable<Float, AnimationVector1D> = Animatable(0f, Float.VectorConverter),
    val offset: Animatable<Float, AnimationVector1D> = Animatable(15f, Float.VectorConverter)
)

data class CellData(
    val value: Int,
    val id: String = UUID.randomUUID().toString()
)

data class GameModel(
    val numRows: Int = 4,
    val numCols: Int = 4,
    val score: Int = 0,
    val bestScore: Int = 0,
    val addedScore: AddedScoreData = AddedScoreData(value = 0),
    val board: MutableMap<Pair<Int, Int>, CellData> = mutableMapOf(),
    val mergedPositions: List<Pair<Int, Int>> = listOf(),
    val newSpawnPositions: List<Pair<Int, Int>> = listOf(),
    val hasLost: Boolean = false
) {
    fun getBoardHashMap(): HashMap<String, CellData> = board.toStringKeyMap()
}