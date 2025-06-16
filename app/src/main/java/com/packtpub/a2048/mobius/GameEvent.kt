package com.packtpub.a2048.mobius

sealed class GameEvent {
    data object SwipeRight: GameEvent()
    data object SwipeLeft: GameEvent()
    data object SwipeUp: GameEvent()
    data object SwipeDown: GameEvent()

    data object StartGame: GameEvent()

    data class InitializeGameFromData(
        val board: MutableBoard,
        val bestScore: Int,
        val score: Int
    ): GameEvent()
}