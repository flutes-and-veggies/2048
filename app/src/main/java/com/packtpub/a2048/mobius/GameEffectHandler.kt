package com.packtpub.a2048.mobius

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.functions.Consumer

class GameEffectHandler: Connectable<GameEffect, GameEvent> {
    override fun connect(output: Consumer<GameEvent>): Connection<GameEffect> {
        return object : Connection<GameEffect> {
            override fun accept(value: GameEffect) {
            }

            override fun dispose() {
            }
        }
    }
}