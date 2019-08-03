package generation.abstracted

import mythic.spatial.Vector3i
import randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute

private val horizontalOffsets = listOf(
    Pair(-1, -1),
    Pair(0, -1),
    Pair(1, -1),
    Pair(-1, 0),
    Pair(1, 0),
    Pair(-1, 1),
    Pair(0, 1),
    Pair(1, 1)
)

private val verticalOffsets = listOf(-1, 0, 1)

private val allOffsets = horizontalOffsets.flatMap { (x, y) ->
  verticalOffsets.map { z -> Vector3i(x, y, z) }
}

fun isCellOpen(grid: MapGrid, from: Vector3i, connectionOffset: Vector3i): Boolean {
  val connectionPosition = from + connectionOffset
  if (grid.connections.containsKey(connectionPosition))
    return false

  return true
}

private fun nextConnectionOffset(dice: Dice, grid: MapGrid, position: Vector3i): Vector3i {
  val z = dice.getInt(-1, 1)
  val options = allOffsets
      .filter { isCellOpen(grid, position, it) }
  return dice.takeOne(options)
}

private tailrec fun addPathStep(maxSteps: Int, dice: Dice, grid: MapGrid, position: Vector3i, stepCount: Int = 0): MapGrid {
  if (stepCount == maxSteps)
    return grid

  val connectionOffset = nextConnectionOffset(dice, grid, position)
  val connectionPosition = position + connectionOffset
  val nextPosition = position + connectionOffset * 2
  val attributes = if (stepCount == maxSteps - 1)
    setOf(NodeAttribute.exit)
  else
    setOf()

  val newGrid = grid.copy(
      cells = grid.cells.plus(
          nextPosition to Cell(attributes = attributes)
      ),
      connections = grid.connections.plus(
          connectionPosition to Pair(position, nextPosition)
      )
  )
  return addPathStep(maxSteps, dice, newGrid, nextPosition, stepCount + 1)
}

fun newWindingPath(dice: Dice): MapGrid {
  val startPosition = Vector3i.zero
  val grid = MapGrid(
      cells = mapOf(
          startPosition to Cell(attributes = setOf(NodeAttribute.home))
      )
  )
  return addPathStep(20, dice, grid, startPosition)
}
