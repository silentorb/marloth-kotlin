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

fun isCellOpen(grid: MapGrid, from: Vector3i, connectionOffset: Vector3i): Boolean =
    !grid.cells.containsKey(from + connectionOffset * 2)

private fun nextConnectionOffset(dice: Dice, grid: MapGrid, position: Vector3i): Vector3i? {
  // Filter out any horizontal directions that already have a connection to this cell.
  val availableOffsets = horizontalOffsets
//      .mapNotNull { (x, y) ->
      .map { (x, y) ->
        //        if (verticalOffsets.all { z ->
//              val offset = Vector3i(x, y, z)
//              val cell = grid.connections[position + offset]
//              cell == null || cell.first != position && cell.second != position
//            })
        verticalOffsets.map { z -> Vector3i(x, y, z) }
//        else null
      }
      .flatten()

  // Further filter out any potential conflicts with straight tunnels and vertical tunnels
  // that connect other cells than this one.
  val options = availableOffsets
      .filter {
        isCellOpen(grid, position, it) &&
            if (it.z != position.z)
              isCellOpen(grid, position, it.copy(z = position.z))
            else
              isCellOpen(grid, position, it.copy(z = position.z + 1))
                  && isCellOpen(grid, position, it.copy(z = position.z - 1))
      }
  return if (options.any())
    dice.takeOne(options)
  else
    null
}

private tailrec fun addPathStep(maxSteps: Int, dice: Dice, grid: MapGrid, position: Vector3i, stepCount: Int = 0): MapGrid {
  if (stepCount == maxSteps)
    return grid

  val connectionOffset = nextConnectionOffset(dice, grid, position)
  if (connectionOffset == null)
    return grid

  val tunnelPosition = position + connectionOffset
  val nextPosition = position + connectionOffset * 2
  val attributes = if (stepCount == maxSteps - 1)
    setOf(NodeAttribute.exit)
  else
    setOf()

  assert(!grid.cells.containsKey(nextPosition))

  val newGrid = grid.copy(
      cells = grid.cells.plus(listOf(
          tunnelPosition to Cell(attributes = setOf(NodeAttribute.tunnel)),
          nextPosition to Cell(attributes = attributes.plus(setOf(NodeAttribute.room)))
      )),
      connections = grid.connections.plus(listOf(
          Pair(position, tunnelPosition),
          Pair(tunnelPosition, nextPosition)
      ))
  )
  return addPathStep(maxSteps, dice, newGrid, nextPosition, stepCount + 1)
}

fun newWindingPath(dice: Dice, length: Int): MapGrid {
  val startPosition = Vector3i(0, 0, 1)
  val grid = MapGrid(
      cells = mapOf(
          startPosition to Cell(attributes = setOf(NodeAttribute.room, NodeAttribute.home))
      )
  )
  return addPathStep(length - 1, dice, grid, startPosition)
}
