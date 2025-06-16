package com.packtpub.a2048.mobius

typealias MutableBoard = MutableMap<Pair<Int, Int>, CellData>
typealias Board = Map<Pair<Int, Int>, CellData>

fun MutableBoard.formatBoard(
    numCols: Int,
    numRows: Int
): String {
    var finalStr = ""
    repeat(numRows) { row ->
        repeat(numCols) { col ->
            finalStr += "${this[Pair(row, col)]!!.value} "
        }
        finalStr += "\n"
    }
    return finalStr.trimMargin()
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