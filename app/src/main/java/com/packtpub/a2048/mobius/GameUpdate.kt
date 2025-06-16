package com.packtpub.a2048.mobius

import com.packtpub.a2048.helpers.GameLogic
import com.spotify.mobius.Next
import com.spotify.mobius.Update

class GameUpdate : Update<GameModel, GameEvent, GameEffect> {
    override fun update(
        model: GameModel,
        event: GameEvent
    ): Next<GameModel, GameEffect> {
        return when (event) {
            GameEvent.SwipeDown -> {
                Next.next(
                    GameLogic.swipeDown(model)
                )
            }
            GameEvent.SwipeLeft -> {
                Next.next(
                    GameLogic.swipeLeft(model)
                )
            }
            GameEvent.SwipeRight -> {
                Next.next(
                    GameLogic.swipeRight(model)
                )
            }
            GameEvent.SwipeUp -> {
                Next.next(
                    GameLogic.swipeUp(model)
                )
            }
            GameEvent.StartGame -> {
                Next.next(
                    GameLogic.spawnTile(
                        board = GameLogic.initializeBoard(model.numRows, model.numCols),
                        model = model,
                        score = 0,
                        numTiles = 2
                    )
                )
            }
            is GameEvent.InitializeGameFromData-> {
                val positions = mutableListOf<Pair<Int, Int>>()
                repeat(model.numRows) { rowIndex ->
                    repeat(model.numCols) { colIndex ->
                        val pos = Pair(rowIndex, colIndex)
                        if ((model.board[pos] == null || model.board[pos]?.value == 0)
                            && event.board[pos]?.value != 0) {
                            positions.add(pos)
                        }
                    }
                }
                Next.next(
                    model.copy(
                        score = event.score,
                        bestScore = Math.max(event.bestScore, event.score),
                        newSpawnPositions = positions,
                        board = event.board
                    )
                )
            }
        }
    }
}

