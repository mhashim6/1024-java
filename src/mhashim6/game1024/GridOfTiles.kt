package mhashim6.game1024

import mhashim6.game1024.Position.PositionComparators.DOWN_ORDER
import mhashim6.game1024.Position.PositionComparators.LEFT_ORDER
import mhashim6.game1024.Position.PositionComparators.RIGHT_ORDER
import mhashim6.game1024.Position.PositionComparators.UP_ORDER
import mhashim6.game1024.Direction.*
import java.util.*

internal class GridOfTiles constructor(private val x: Int, private val y: Int = x) {

	private var gridOfTiles: Array<Array<Tile?>> = Array(x) { arrayOfNulls<Tile>(y) }
	private var snapshotTiles: Array<Array<Tile?>> = Array(x) { arrayOfNulls<Tile>(y) } // for undo

	private val rnd: Random = Random()

	var currentMax = 2
		private set

	fun copy() = gridOfTiles.copyOf()

	fun takeSnapshot() {
		clearSnapshots()
		gridOfTiles.forEachIndexed2d { i, j, tile: Tile? ->
			if (tile != null)
				snapshotTiles[i][j] = TileImpl(tile)
		}
	}

	fun useSnapshot() {
		clear()
		snapshotTiles.forEachIndexed2d { i, j, tile: Tile? ->
			if (tile != null)
				gridOfTiles[i][j] = TileImpl(tile)
		}
	}

	fun newTile() {
		if (emptySpaces.isEmpty())
			return

		val emptySpaces = emptySpaces
		val index = rnd.nextInt(emptySpaces.size)
		add(TileImpl(emptySpaces[index]))
	}

	private fun add(tile: Tile?) {

		val pos = tile!!.position

		val row = pos.row
		val col = pos.col
		gridOfTiles[row][col] = tile
	}

	private fun remove(pos: Position) {
		gridOfTiles[pos.row][pos.col] = null
	}

	private fun getTile(pos: Position) = getTile(pos.row, pos.col)

	private fun getTile(row: Int, col: Int) = gridOfTiles[row][col]

	private fun isPositionEmpty(position: Position) = (gridOfTiles[position.row][position.col] == null)
	// ============================================================

	fun migrate(direction: Direction) {
		val filledPositions = filledPositions
		sortByDirection(filledPositions, direction)

		for (pos in filledPositions)
			migrateSingle(pos, direction)
	}

	private fun sortByDirection(positions: MutableList<Position>, direction: Direction) {
		when (direction) {
			UP -> positions.sortWith(UP_ORDER)
			DOWN -> positions.sortWith(DOWN_ORDER)
			RIGHT -> positions.sortWith(RIGHT_ORDER)
			LEFT -> positions.sortWith(LEFT_ORDER)
		}
	}

	private fun migrateSingle(current: Position, direction: Direction) {
		val newPos = getFurthermost(current, direction)
		tryReposition(current, newPos)
	}

	private fun getFurthermost(original: Position, direction: Direction): Position {
		return getFurthermost(original, original, direction)
	}

	private fun getFurthermost(original: Position, currentValid: Position, direction: Direction): Position {
		var row = currentValid.row
		var col = currentValid.col

		when (direction) {
			UP ->
				if (currentValid.row == 0) //minimum row
					return currentValid //we can't move further.
				else
					row -= 1

			DOWN ->
				if (currentValid.row == x - 1) //max row
					return currentValid //we can't move further.
				else
					row += 1

			RIGHT ->
				if (currentValid.col == y - 1)  //max col
					return currentValid //we can't move further.
				else
					col += 1

			LEFT ->
				if (currentValid.col == 0)  //minimum col
					return currentValid //we can't move further.
				else
					col -= 1
		}
		val newPos = Position(row, col)

		if (isPositionEmpty(newPos)) { // check for the next position
			return getFurthermost(original, newPos, direction)

		} else if (compareTiles(original, newPos) == 0) //these tiles have the same value
			return newPos

		return currentValid //we can't move further.
	}

	private fun tryReposition(current: Position, target: Position) {
		when {
			current == target -> return
			isPositionEmpty(target) -> reposition(current, target)
			else -> merge(current, target)
		}
	}

	private fun reposition(current: Position, target: Position) {
		val tile = getTile(current)
		remove(current)
		tile!!.position = target
		add(tile)
	}

	private fun merge(pos: Position, target: Position) {
		remove(pos)
		currentMax = getTile(target)!!.x2()
	}

	internal fun clear() {
		gridOfTiles = Array(x) { arrayOfNulls<Tile>(y) }
	}

	private fun clearSnapshots() {
		snapshotTiles = Array(x) { arrayOfNulls<Tile>(y) }
	}
	// ============================================================

	private val emptySpaces: List<Position>
		get() {
			val emptySpaces = mutableListOf<Position>()
			gridOfTiles.forEachIndexed2d { i, j, tile ->
				if (tile == null)
					emptySpaces.add(Position(i, j))
			}
			return emptySpaces
		}

	private val filledPositions: MutableList<Position>
		get() {
			val positions = ArrayList<Position>()
			gridOfTiles.forEachIndexed2d { i, j, tile ->
				if (tile != null)
					positions.add(Position(i, j))
			}
			return positions
		}

	val isMovingPossible: Boolean
		get() {
			if (emptySpaces.isNotEmpty())
				return true

			for (i in 0 until x)
				for (j in 0 until y - 1) {
					if (compareTiles(i, j, i, j + 1) == 0) //compare neighbors in the same row.
						return true
					if (compareTiles(j, i, j + 1, i) == 0) //compare neighbors in the same column.
						return true
				}
			return false
		}
	// ============================================================

	private fun compareTiles(pos1: Position, pos2: Position): Int? {
		return compareTiles(pos1.row, pos1.col, pos2.row, pos2.col)
	}

	private fun compareTiles(row1: Int, col1: Int, row2: Int, col2: Int): Int? {
		return compareTiles(gridOfTiles[row1][col1], gridOfTiles[row2][col2])
	}

	private fun compareTiles(tile: Tile?, another: Tile?): Int? {
		return tile!!.compareTo(another!!)
	}

}
