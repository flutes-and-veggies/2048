package com.packtpub.a2048

import com.packtpub.a2048.helpers.GameLogic
import com.packtpub.a2048.mobius.CellData
import com.packtpub.a2048.mobius.GameModel
import com.packtpub.a2048.mobius.MutableBoard
import com.packtpub.a2048.mobius.formatBoard
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe

class GameLogicTest : FunSpec({
    fun generateBoard(
        numRows: Int = NUM_ROWS,
        numCols: Int = NUM_COLS,
        value: Int = 0
    ): MutableBoard {
        val result = mutableMapOf<Pair<Int, Int>, CellData>()

        repeat(numRows) { row ->
            repeat(numCols) { col ->
                result[Pair(row, col)] = CellData(value = value)
            }
        }
        return result
    }

    fun generateUnmergableUnswipeableBoard(): MutableBoard {
        /*
            2 4 2
            4 2 4
            2 4 2
         */
        val board = generateBoard()
        // 2 - (0, 0), (0, 2), (1, 1), (2, 0), (2, 2)
        // 4 - (0, 1), (1, 0), (1, 2), (2, 1)
        listOf(
            Pair(0, 0),
            Pair(0, 2),
            Pair(1, 1),
            Pair(2, 0),
            Pair(2, 2)
        ).forEach {
            board[it] = CellData(value = 2)
        }
        listOf(
            Pair(0, 1),
            Pair(1, 0),
            Pair(1, 2),
            Pair(2, 1),
        ).forEach {
            board[it] = CellData(value = 4)
        }
        return board
    }

    context("swipeUp") {
        test("does nothing when board is full and nothing can be merged") {
            val board = generateUnmergableUnswipeableBoard()
            val result = GameLogic.swipeUp(
                model = GameModel(
                        numCols = NUM_COLS,
                        numRows = NUM_ROWS
                    ).copy(
                        board = board
                    ),
                shouldSpawn = false
            )

            result.board shouldBe board
        }

        test("moves all board values as high up as possible") {
            /*
                0 0 0
                0 2 0
                4 8 4
             */
            val board = generateBoard(value = 0)
            board[Pair(2, 0)] = CellData(value = 4)
            board[Pair(2, 1)] = CellData(value = 8)
            board[Pair(2, 2)] = CellData(value = 4)
            board[Pair(1, 1)] = CellData(value = 2)

            /*
                4 2 4
                0 8 0
                0 0 0
            */
            val result = GameLogic.swipeUp(
                model = GameModel().copy(
                    board = board,
                    numRows = NUM_ROWS,
                    numCols = NUM_COLS
                ),
                shouldSpawn = false
            )
            result.board.formatBoard(
                numCols = NUM_COLS,
                numRows = NUM_ROWS
            ) shouldBe "4 2 4 \n" + "0 8 0 \n" + "0 0 0 "
        }

        test("does not move tiles that are already on top") {
            /*
                0 4 0
                0 0 0
                4 8 4
             */
            val board = generateBoard()
            board[Pair(0, 1)] = CellData(value = 4)
            board[Pair(2, 0)] = CellData(value = 4)
            board[Pair(2, 1)] = CellData(value = 8)
            board[Pair(2, 2)] = CellData(value = 4)

            val result = GameLogic.swipeUp(
                model = GameModel().copy(
                    board = board,
                    numRows = NUM_ROWS,
                    numCols = NUM_COLS
                ),
                shouldSpawn = false
            )

            result.board.formatBoard(
                numRows = NUM_ROWS,
                numCols = NUM_COLS
            ) shouldBe "4 4 4 \n" + "0 8 0 \n" + "0 0 0 "
        }

        test("merges tiles together") {
            /*
                0 2 0
                0 2 0
                0 2 0
             */
            val board = generateBoard(value = 0)
            board[Pair(0, 1)] = CellData(value = 2)
            board[Pair(1, 1)] = CellData(value = 2)
            board[Pair(2, 1)] = CellData(value = 2)

            val result = GameLogic.swipeUp(
                model = GameModel().copy(
                    board = board,
                    numRows = NUM_ROWS,
                    numCols = NUM_COLS
                ),
                shouldSpawn = false
            )

            result.board.formatBoard(
                numRows = NUM_ROWS,
                numCols = NUM_COLS
            ) shouldBe "0 4 0 \n" + "0 2 0 \n" + "0 0 0 "
        }
    }

    context("swipeDown") {
        test("does nothing when board is full and nothing can be merged") {
            val board = generateUnmergableUnswipeableBoard()
            val result = GameLogic.swipeDown(model = GameModel(
                numCols = NUM_COLS,
                numRows = NUM_ROWS
            ).copy(
                board = board
            ))

            result.board shouldBe board
        }
    }

    context("swipeRight") {
        test("does nothing when board is full and nothing can be merged") {
            val board = generateUnmergableUnswipeableBoard()
            val result = GameLogic.swipeRight(model = GameModel(
                numCols = NUM_COLS,
                numRows = NUM_ROWS
            ).copy(
                board = board
            ))

            result.board shouldBe board
        }
    }

    context("swipeLeft") {
        test("does nothing when board is full and nothing can be merged") {
            val board = generateUnmergableUnswipeableBoard()
            val result = GameLogic.swipeLeft(model = GameModel(
                numCols = NUM_COLS,
                numRows = NUM_ROWS
            ).copy(
                board = board
            ))

            result.board shouldBe board
        }
    }

    context("hasLost") {
        test("returns false when the board is empty") {
            GameLogic.hasLost(
                NUM_ROWS,
                NUM_COLS,
                board = generateBoard()
            ) shouldBe false
        }

        test("returns false when the board is full except one tile") {
            val board = generateBoard()
            board[Pair(0, 0)] = CellData(value = 2)
            GameLogic.hasLost(
                NUM_ROWS,
                NUM_COLS,
                board = board
            ) shouldBe false
        }

        test("returns true when the board is full and none of the adjacent tiles are the same") {
            val board = generateUnmergableUnswipeableBoard()
            GameLogic.hasLost(
                NUM_ROWS,
                NUM_COLS,
                board = board
            ) shouldBe true
        }

        test("returns false when the board is full but one of the adjacent tiles has an equal value neighbor") {
            /*
               2 4 2
               4 2 4
               8 8 2
            */
            val board = generateUnmergableUnswipeableBoard()
            board[Pair(2, 1)] = CellData(value = 8)
            board[Pair(2, 0)] = CellData(value = 8)
            GameLogic.hasLost(
                NUM_ROWS,
                NUM_COLS,
                board = board
            ) shouldBe false
        }
    }

    context("spawnTile") {
        test("does not spawn tile in board when it is full") {
            val starterBoard = generateBoard(value = 2)
            val result = GameLogic.spawnTile(
                model = GameModel(),
                board = starterBoard,
                numTiles = 1
            )
            result.board shouldBe starterBoard
        }

        test("spawns tile in board when it is not full of value 2 or 4") {
            val starterBoard = generateBoard(value = 2)
            starterBoard[Pair(0, 0)] = CellData(value = 0)
            val result = GameLogic.spawnTile(
                model = GameModel(),
                board = starterBoard,
                numTiles = 1
            )
            result.board[Pair(0, 1)]?.value shouldBe 2

            val spawnedValue = result.board[Pair(0, 0)]?.value
            spawnedValue shouldBeIn arrayOf(2, 4)
        }
    }
}) {
    companion object {
        const val NUM_ROWS = 3
        const val NUM_COLS = 3
    }
}