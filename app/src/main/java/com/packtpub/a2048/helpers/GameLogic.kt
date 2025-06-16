package com.packtpub.a2048.helpers

import com.packtpub.a2048.mobius.AddedScoreData
import com.packtpub.a2048.mobius.CellData
import com.packtpub.a2048.mobius.GameModel
import com.packtpub.a2048.mobius.MutableBoard
import com.packtpub.a2048.mobius.isEqual
import com.packtpub.a2048.mobius.toSortedAscendingCol
import com.packtpub.a2048.mobius.toSortedAscendingRow
import com.packtpub.a2048.mobius.toSortedDescendingCol
import com.packtpub.a2048.mobius.toSortedDescendingRow
import java.util.UUID
import kotlin.random.Random

object GameLogic {
    /*
        A user has lost if no tiles are empty and no adjacent tiles have the same value
     */
    fun hasLost(
        numRows: Int,
        numCols: Int,
        board: MutableBoard
    ): Boolean {
        val hasEmptySpaces = board.values.any { it.value === 0 }
        if (hasEmptySpaces) { return false }
        // for each tile, check upper, left, right, and lower neighbor
        var hasAnyMergableNeighbors = false
        for (key in board.keys) {
            val (row, col) = key
            var hasMergableNeighbor = false
            // upper neighbor
            if (row > 0) {
                hasMergableNeighbor = hasMergableNeighbor ||
                    (board[Pair(row - 1, col)]!!.value == board[key]!!.value)
            }
            // left neighbor
            if (col > 0) {
                hasMergableNeighbor = hasMergableNeighbor ||
                    (board[Pair(row, col - 1)]!!.value == board[key]!!.value)
            }
            // right neighbor
            if (col + 1 < numCols) {
                hasMergableNeighbor = hasMergableNeighbor ||
                    (board[Pair(row, col + 1)]!!.value == board[key]!!.value)
            }
            // lower neighbor
            if (row + 1 < numRows) {
                hasMergableNeighbor = hasMergableNeighbor ||
                    (board[Pair(row + 1, col)]!!.value == board[key]!!.value)
            }

            hasAnyMergableNeighbors = hasAnyMergableNeighbors || hasMergableNeighbor
        }
        return !hasAnyMergableNeighbors
    }

    fun swipeUp(
        model: GameModel,
        shouldSpawn: Boolean = true
    ): GameModel {
        val oldBoard = model.board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = model.board.toMutableMap().toSortedAscendingRow()
        for (key in boardCopy.keys) {
            val (row, col) = key
            val oldVal = boardCopy[key]!!
            if (oldVal.value == 0) { continue }
            var newRow = row
            while (newRow - 1 >= 0 && boardCopy[Pair(newRow - 1, col)]?.value == 0) {
                newRow -= 1
            }
            if (newRow != row) {
                boardCopy[Pair(newRow, col)] = oldVal
                boardCopy[Pair(row, col)] = CellData(value = 0)
            }
        }
        var finalScore = model.score
        // ---- merge logic ------
        for (key in boardCopy.keys) {
            val (tileRow, tileCol) = key
            val tile = boardCopy[key]!!

            if (tileRow - 1 >= 0) {
                // if upper neighbor is the same merge
                val neighborTile = boardCopy[Pair(tileRow - 1, tileCol)]!!
                if (tile.value == neighborTile.value) {
                    boardCopy[key] = CellData(value = 0)
                    val mergedPos = Pair(tileRow - 1, tileCol)
                    boardCopy[mergedPos] = CellData(
                        value = tile.value + neighborTile.value,
                        id = tile.id
                    )
                    mergedPositions.add(mergedPos)
                    finalScore += tile.value + neighborTile.value

                    // move everything below the tile up
                    var currRow = model.numRows - 1
                    while (currRow - 1 >= tileRow) {
                        boardCopy[Pair(currRow - 1, tileCol)] = boardCopy[Pair(currRow, tileCol)]!!
                        currRow -= 1
                    }
                    boardCopy[Pair(model.numRows - 1, tileCol)] = CellData(value = 0)
                }
            }
        }
        return updateBoard(
            model = model,
            shouldSpawn = shouldSpawn,
            mergedPositions = mergedPositions,
            finalScore = finalScore,
            oldBoard = oldBoard,
            boardCopy = boardCopy,
            numRows = model.numRows,
            numCols = model.numCols,
            hasLost = hasLost(
                model.numRows, model.numCols, boardCopy
            )
        )
    }

    fun swipeDown(
        model: GameModel,
        shouldSpawn: Boolean = true
    ): GameModel {
        val oldBoard = model.board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = model.board.toMutableMap().toSortedDescendingRow()
        for (key in boardCopy.keys) {
            val (row, col) = key
            val oldVal = boardCopy[key]!!
            if (oldVal.value == 0) {
                continue
            }
            var newRow = row
            while (newRow + 1 < model.numRows && boardCopy[Pair(newRow + 1, col)]?.value == 0) {
                newRow += 1
            }
            if (newRow != row) {
                boardCopy[Pair(newRow, col)] = oldVal
                boardCopy[Pair(row, col)] = CellData(value = 0)
            }
        }
        var finalScore = model.score
        // ---- merge logic ------
        for (key in boardCopy.keys) {
            val (tileRow, tileCol) = key
            val tile = boardCopy[key]!!

            if (tileRow + 1 < model.numRows) {
                // if lower neighbor is the same merge
                val neighborTile = boardCopy[Pair(tileRow + 1, tileCol)]!!
                if (tile.value == neighborTile.value) {
                    boardCopy[key] = CellData(value = 0)
                    val mergedPos = Pair(tileRow + 1, tileCol)
                    boardCopy[mergedPos] = CellData(
                        value = tile.value + neighborTile.value,
                        id = tile.id
                    )
                    mergedPositions.add(mergedPos)
                    finalScore += tile.value + neighborTile.value

                    // move everything above the tile down
                    var currRow = 0
                    while (currRow + 1 < tileRow) {
                        boardCopy[Pair(currRow + 1, tileCol)] = boardCopy[Pair(currRow, tileCol)]!!
                        currRow += 1
                    }
                    boardCopy[Pair(0, tileCol)] = CellData(value = 0)
                }
            }
        }
        return updateBoard(
            model = model,
            mergedPositions = mergedPositions,
            finalScore = finalScore,
            oldBoard = oldBoard,
            boardCopy = boardCopy,
            numRows = model.numRows,
            numCols = model.numCols,
            shouldSpawn = shouldSpawn,
            hasLost = hasLost(
                model.numRows, model.numCols, boardCopy
            )
        )
    }

    fun swipeRight(
        model: GameModel,
        shouldSpawn: Boolean = true
    ): GameModel {
        val oldBoard = model.board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = model.board.toMutableMap().toSortedDescendingCol()
        for (key in boardCopy.keys) {
            val (row, col) = key
            val oldVal = boardCopy[key]!!
            if (oldVal.value == 0) { continue }
            var newCol = col
            while (newCol + 1 < model.numCols && boardCopy[Pair(row, newCol + 1)]?.value == 0) {
                newCol += 1
            }
            if (newCol != col) {
                boardCopy[Pair(row, newCol)] = oldVal
                boardCopy[Pair(row, col)] = CellData(value = 0)
            }
        }
        var finalScore = model.score
        // ---- merge logic ------
        for (key in boardCopy.keys) {
            val (tileRow, tileCol) = key
            val tile = boardCopy[key]!!

            if (tileCol + 1 < model.numCols) {
                // if right neighbor is the same merge
                val neighborTile = boardCopy[Pair(tileRow, tileCol + 1)]!!
                if (tile.value == neighborTile.value) {
                    boardCopy[key] = CellData(value = 0)
                    val mergedPos = Pair(tileRow, tileCol + 1)
                    boardCopy[mergedPos] = CellData(
                        value = tile.value + neighborTile.value,
                        id = tile.id
                    )
                    mergedPositions.add(mergedPos)
                    finalScore += tile.value + neighborTile.value

                    // move everything to the left of tile to the right
                    var currCol = 0
                    while (currCol + 1 < tileCol) {
                        boardCopy[Pair(tileRow, currCol + 1)] = boardCopy[Pair(tileRow, currCol)]!!
                        currCol = currCol + 1
                    }
                    boardCopy[Pair(tileRow, 0)] = CellData(value = 0)
                }
            }
        }
        return updateBoard(
            model = model,
            shouldSpawn = shouldSpawn,
            mergedPositions = mergedPositions,
            finalScore = finalScore,
            oldBoard = oldBoard,
            boardCopy = boardCopy,
            numRows = model.numRows,
            numCols = model.numCols,
            hasLost = hasLost(
                model.numRows, model.numCols, boardCopy
            )
        )
    }

    fun swipeLeft(
        model: GameModel,
        shouldSpawn: Boolean = true
    ): GameModel {
        val oldBoard = model.board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = model.board.toMutableMap().toSortedAscendingCol()
        for (key in boardCopy.keys) {
            val (row, col) = key
            val oldVal = boardCopy[key]!!
            if (oldVal.value == 0) { continue }
            var newCol = col
            while (newCol - 1 >= 0 && boardCopy[Pair(row, newCol - 1)]?.value == 0) {
                newCol -= 1
            }
            if (newCol != col) {
                boardCopy[Pair(row, newCol)] = oldVal
                boardCopy[Pair(row, col)] = CellData(value = 0)
            }
        }
        var finalScore: Int = model.score
        // ---- merge logic -----
        for (key in boardCopy.keys) {
            val (tileRow, tileCol) = key
            val tile = boardCopy[key]!!

            if (tileCol - 1 >= 0) {
                // if left neighbor is the same merge
                val neighborTile = boardCopy[Pair(tileRow, tileCol - 1)]!!
                if (tile.value == neighborTile.value) {
                    boardCopy[key] = CellData(value = 0)
                    val mergedPos = Pair(tileRow, tileCol - 1)
                    boardCopy[mergedPos] = CellData(
                        value = tile.value + neighborTile.value,
                        id = tile.id
                    )
                    mergedPositions.add(mergedPos)
                    finalScore += tile.value + neighborTile.value

                    // move everything to the right of tile to the left
                    var currCol = tileCol
                    while (currCol < model.numCols - 1) {
                        boardCopy[Pair(tileRow, currCol)] = boardCopy[Pair(tileRow, currCol + 1)]!!
                        currCol = currCol + 1
                    }
                    boardCopy[Pair(tileRow, model.numCols - 1)] = CellData(value = 0)
                }
            }
        }
        return updateBoard(
            model = model,
            mergedPositions = mergedPositions,
            finalScore = finalScore,
            oldBoard = oldBoard,
            boardCopy = boardCopy,
            numRows = model.numRows,
            numCols = model.numCols,
            shouldSpawn = shouldSpawn,
            hasLost = hasLost(
                model.numRows, model.numCols, boardCopy
            )
        )
    }

    private fun updateBoard(
        shouldSpawn: Boolean,
        model: GameModel,
        hasLost: Boolean,
        mergedPositions: MutableList<Pair<Int, Int>>,
        finalScore: Int,
        oldBoard: MutableBoard,
        boardCopy: MutableBoard,
        numRows: Int,
        numCols: Int
    ): GameModel {
        val changed = !oldBoard.isEqual(boardCopy, numRows, numCols)
        val addedScore = if (finalScore > model.score) AddedScoreData(
            value = finalScore - model.score
        ) else null
        return if (shouldSpawn && changed) {
            spawnTile(
                model = model,
                hasLost = hasLost,
                board = boardCopy,
                addedScore = addedScore,
                mergedPositions = mergedPositions,
                score = finalScore
            )
        } else if (changed) {
            model.copy(
                hasLost = hasLost,
                board = boardCopy,
                mergedPositions = mergedPositions,
                score = finalScore,
                addedScore = addedScore ?: model.addedScore
            )
        } else {
            model
        }
    }

    fun initializeBoard(
        numRows: Int,
        numCols: Int
    ): MutableBoard {
        val newBoard: MutableBoard = mutableMapOf()
        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                newBoard[Pair(i, j)] = CellData(value = 0)
            }
        }
        return newBoard
    }

    fun spawnTile(
        model: GameModel,
        hasLost: Boolean? = null,
        addedScore: AddedScoreData? = null,
        board: MutableBoard? = null,
        score: Int? = null,
        mergedPositions: MutableList<Pair<Int, Int>>? = null,
        numTiles: Int = 1
    ): GameModel {
        val positions = mutableListOf<Pair<Int, Int>>()
        val boardCopy = board ?: model.board
        val score = score ?: model.score
        repeat(numTiles) {
            val availableSpaces = boardCopy.keys.filter { boardCopy[it]?.value == 0 }
            // No more empty squares
            if (availableSpaces.size == 0) {
                return model.copy(
                    score = score,
                    board = boardCopy,
                    hasLost = hasLost ?: false,
                    bestScore = Math.max(model.bestScore, score),
                    addedScore = addedScore ?: model.addedScore,
                    mergedPositions = mergedPositions ?: model.mergedPositions,
                )
            }
            val nextSpawnValue = Random.nextInt(10) + 1
            var tile = 2
            if (nextSpawnValue == 10) {
                tile = 4
            }

            val nextSpawnPosition = Random.nextInt(availableSpaces.size)
            val nextPos = availableSpaces[nextSpawnPosition]
            val currentId = boardCopy[nextPos]?.id
            boardCopy[nextPos] = CellData(
                value = tile,
                id = currentId ?: UUID.randomUUID().toString()
            )
            positions.add(nextPos)
        }

        return model.copy(
            score = score,
            board = boardCopy,
            hasLost = hasLost ?: false,
            bestScore = Math.max(model.bestScore, score),
            addedScore = addedScore ?: model.addedScore,
            mergedPositions = mergedPositions ?: model.mergedPositions,
            newSpawnPositions = positions
        )
    }
}