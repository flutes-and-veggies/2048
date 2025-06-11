package com.packtpub.a2048.mobius

import com.spotify.mobius.Next
import com.spotify.mobius.Update

class GameUpdate : Update<GameModel, GameEvent, GameEffect> {
    override fun update(
        model: GameModel,
        event: GameEvent
    ): Next<GameModel, GameEffect> {
        return when (event) {
            GameEvent.SwipeDown -> {
                Next.noChange()
            }
            GameEvent.SwipeLeft -> {
                Next.noChange()
            }
            GameEvent.SwipeRight -> {
                Next.noChange()
            }
            GameEvent.SwipeUp -> {
                Next.noChange()
            }
            GameEvent.StartGame -> {
                Next.noChange()
            }
        }
    }
}