package generation.abstracted

import mythic.spatial.Vector3i
import randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute

private val horizontalDiagonalOffsets = setOf(
    Pair(-1, -1),
    Pair(1, -1),
    Pair(-1, 1),
    Pair(1, 1)
)

private val horizontalOrthogonalOffsets = setOf(
    Pair(0, -1),
    Pair(-1, 0),
    Pair(1, 0),
    Pair(0, 1)
)

//private val horizontalOffsets = horizontalOffsetsDiagonal.plus(horizontalOffsetsOrthogonal)

private val verticalOffsets = setOf(-1, 0, 1)

//private val allOffsets = horizontalOffsets.flatMap { (x, y) ->
//  verticalOffsets.map { z -> Vector3i(x, y, z) }
//}

private fun nextConnectionOffset(dice: Dice, grid: MapGrid, position: Vector3i): Vector3i? {
  val availableOffsets = horizontalDiagonalOffsets
      .flatMap { (x, y) -> verticalOffsets.map { z -> Vector3i(x, y, z) } }
      .plus(horizontalOrthogonalOffsets.map { (x, y) -> Vector3i(x, y, 0) })

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
