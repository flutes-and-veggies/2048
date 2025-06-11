package com.packtpub.a2048.viewmodel

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.VectorConverter
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import kotlin.random.Random

data class CellData(
    val value: Int,
    val id: String = UUID.randomUUID().toString()
)

data class AddedScoreData(
    val value: Int,
    val opacity: Animatable<Float, AnimationVector1D> = Animatable(0f, Float.VectorConverter),
    val offset: Animatable<Float, AnimationVector1D> = Animatable(15f, Float.VectorConverter)
)

typealias MutableBoard = MutableMap<Pair<Int, Int>, CellData>
typealias Board = Map<Pair<Int, Int>, CellData>

class GameViewModel(
    val numRows: Int,
    val numCols: Int
): ViewModel() {
    private val _addedScore: MutableStateFlow<AddedScoreData> = MutableStateFlow(
        AddedScoreData(value = 0)
    )
    val addedScore: StateFlow<AddedScoreData> = _addedScore

    private val _bestScoreFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val bestScoreFlow: StateFlow<Int> = _bestScoreFlow

    private val _scoreFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val scoreFlow: StateFlow<Int> = _scoreFlow

    private val _boardFlow: MutableStateFlow<Board> = MutableStateFlow(
        buildMap {
            repeat(numRows) { row ->
                repeat(numCols) { col ->
                    put(Pair(row, col), CellData(value = 0))
                }
            }
        }
    )

    val _newSpawnPositions: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(listOf(Pair(-1, -1)))
    val newSpawnPositions: StateFlow<List<Pair<Int, Int>>> = _newSpawnPositions

    val _mergedPositions: MutableStateFlow<List<Pair<Int, Int>>> = MutableStateFlow(listOf(Pair(-1, -1)))
    val mergedPositions: StateFlow<List<Pair<Int, Int>>> = _mergedPositions

    val boardFlow: StateFlow<Board> = _boardFlow
    private var board: MutableBoard = mutableMapOf()
        set(value) {
            field = value
            _boardFlow.tryEmit(value)
        }

    private fun updateBoard(position: Pair<Int, Int>, value: Int) {
        val currentId = board[position]?.id
        board[position] = CellData(
            value = value,
            id = currentId ?: UUID.randomUUID().toString()
        )
        _boardFlow.tryEmit(board.toMap())
    }

    fun startGame() {
        _scoreFlow.tryEmit(0)
        initializeBoard()
        spawnTile(2)
    }

    /**
     * Initializes all positions on the board to 0
     */
    fun initializeBoard() {
        val newBoard: MutableBoard = mutableMapOf()
        for (i in 0 until numRows) {
            for (j in 0 until numCols) {
                newBoard[Pair(i, j)] = CellData(value = 0)
            }
        }
        board = newBoard
        _boardFlow.tryEmit(board.toMap())
    }

    /**
     * Generates a new tile in a random position on the board
     *
     * @return True if the tile was spawned, false if it was not
     */
    fun spawnTile(numTiles: Int = 1): Boolean {
        val positions = mutableListOf<Pair<Int, Int>>()
        repeat(numTiles) {
            val availableSpaces = board.keys.filter { board[it]?.value == 0 }
            // No more empty squares
            if (availableSpaces.size == 0) { return false }

            val nextSpawnValue = Random.nextInt(10) + 1
            var tile = 2
            if (nextSpawnValue == 10) {
                tile = 4
            }

            val nextSpawnPosition = Random.nextInt(availableSpaces.size)
            val nextPos = availableSpaces[nextSpawnPosition]
            updateBoard(
                nextPos, tile
            )
            positions.add(nextPos)
        }

        _newSpawnPositions.tryEmit(positions)
        return true
    }

    fun getBoardHashMap(): HashMap<String, CellData> = board.toStringKeyMap()

    fun setNewBoard(newMap: MutableBoard) {
        val positions = mutableListOf<Pair<Int, Int>>()
        repeat(numRows) { rowIndex ->
            repeat(numCols) { colIndex ->
                val pos = Pair(rowIndex, colIndex)
                if (board[pos]?.value == 0 && newMap[pos]?.value != 0) {
                    positions.add(pos)
                }
            }
        }
        _newSpawnPositions.tryEmit(positions)
        board = newMap
    }

    fun setScore(score: Int, showAddedScore: Boolean = true) {
        val addedValue = score - _scoreFlow.value
        if (showAddedScore && addedValue > 0) {
            _addedScore.tryEmit(AddedScoreData(addedValue))
        }
        _scoreFlow.tryEmit(score)

        val currentBest = _bestScoreFlow.value
        if (score > currentBest) {
            _bestScoreFlow.tryEmit(score)
        }
    }

    fun setBestScore(score: Int) {
        _bestScoreFlow.tryEmit(score)
    }

    fun swipeLeft() {
        val oldBoard = board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = board.toMutableMap().toSortedAscendingCol()
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
                    setScore(_scoreFlow.value + tile.value + neighborTile.value)

                    // move everything to the right of tile to the left
                    var currCol = tileCol
                    while (currCol < numCols - 1) {
                        boardCopy[Pair(tileRow, currCol)] = boardCopy[Pair(tileRow, currCol + 1)]!!
                        currCol = currCol + 1
                    }
                    boardCopy[Pair(tileRow, numCols - 1)] = CellData(value = 0)
                }
            }
        }
        if (!oldBoard.isEqual(boardCopy, numRows, numCols)) {
            _mergedPositions.tryEmit(mergedPositions)
            board = boardCopy
            spawnTile()
        }
        printBoard()
    }

    fun swipeRight() {
        val oldBoard = board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = board.toMutableMap().toSortedDescendingCol()
        for (key in boardCopy.keys) {
            val (row, col) = key
            val oldVal = boardCopy[key]!!
            if (oldVal.value == 0) { continue }
            var newCol = col
            while (newCol + 1 < numCols && boardCopy[Pair(row, newCol + 1)]?.value == 0) {
                newCol += 1
            }
            if (newCol != col) {
                boardCopy[Pair(row, newCol)] = oldVal
                boardCopy[Pair(row, col)] = CellData(value = 0)
            }
        }
        // ---- merge logic ------
        for (key in boardCopy.keys) {
            val (tileRow, tileCol) = key
            val tile = boardCopy[key]!!

            if (tileCol + 1 < numCols) {
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
                    setScore(_scoreFlow.value + tile.value + neighborTile.value)

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
        if (!oldBoard.isEqual(boardCopy, numRows, numCols)) {
            _mergedPositions.tryEmit(mergedPositions)
            board = boardCopy
            spawnTile()
        }
        printBoard()
    }

    fun swipeDown() {
        val oldBoard = board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = board.toMutableMap().toSortedDescendingRow()
        for (key in boardCopy.keys) {
            val (row, col) = key
            val oldVal = boardCopy[key]!!
            if (oldVal.value == 0) { continue }
            var newRow = row
            while (newRow + 1 < numRows && boardCopy[Pair(newRow + 1, col)]?.value == 0) {
                newRow += 1
            }
            if (newRow != row) {
                boardCopy[Pair(newRow, col)] = oldVal
                boardCopy[Pair(row, col)] = CellData(value = 0)
            }
        }
        // ---- merge logic ------
        for (key in boardCopy.keys) {
            val (tileRow, tileCol) = key
            val tile = boardCopy[key]!!

            if (tileRow + 1 < numRows) {
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
                    setScore(_scoreFlow.value + tile.value + neighborTile.value)

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
        if (!oldBoard.isEqual(boardCopy, numRows, numCols)) {
            _mergedPositions.tryEmit(mergedPositions)
            board = boardCopy
            spawnTile()
        }
        printBoard()
    }

    fun swipeUp() {
        val oldBoard = board
        val mergedPositions: MutableList<Pair<Int, Int>> = mutableListOf()
        val boardCopy = board.toMutableMap().toSortedAscendingRow()
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
                    setScore(_scoreFlow.value + tile.value + neighborTile.value)

                    // move everything below the tile up
                    var currRow = numRows - 1
                    while (currRow - 1 >= tileRow) {
                        boardCopy[Pair(currRow - 1, tileCol)] = boardCopy[Pair(currRow, tileCol)]!!
                        currRow -= 1
                    }
                    boardCopy[Pair(numRows - 1, tileCol)] = CellData(value = 0)
                }
            }
        }
        if (!oldBoard.isEqual(boardCopy, numRows, numCols)) {
            _mergedPositions.tryEmit(mergedPositions)
            board = boardCopy
            spawnTile()
        }
        printBoard()
    }

    fun printBoard() {
        Log.d("board", board.toString())
    }
}

fun MutableBoard.isEqual(
    board: MutableBoard,
    numRows: Int,
    numCols: Int
): Boolean {
    repeat(numCols) { col ->
        repeat(numRows) { row ->
            if (this[Pair(row, col)]!!.value != board[Pair(row, col)]!!.value) {
                return false
            }
        }
    }

    return true
}

fun MutableBoard.toStringKeyMap(): HashMap<String, CellData> =
    HashMap(mapKeys { (a, _) -> "${a.first}, ${a.second}" })

fun MutableBoard.toSortedAscendingCol(): MutableBoard = this.toSortedMap(
    compareBy<Pair<Int, Int>> { it.second }.thenBy { it.first }
)

fun MutableBoard.toSortedDescendingCol(): MutableBoard = this.toSortedMap(
    compareByDescending<Pair<Int, Int>> { it.second }.thenBy { it.first }
)

fun MutableBoard.toSortedDescendingRow(): MutableBoard = this.toSortedMap(
    compareByDescending<Pair<Int, Int>> { it.first }.thenBy { it.second }
)

fun MutableBoard.toSortedAscendingRow(): MutableBoard = this.toSortedMap(
    compareBy<Pair<Int, Int>> { it.first }.thenBy { it.second }
)