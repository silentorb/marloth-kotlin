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

private val upAndDown = setOf(
    upVector,
    downVector
)
//private val allOffsets = horizontalOffsets.flatMap { (x, y) ->
//  verticalOffsets.map { z -> Vector3i(x, y, z) }
//}

fun distanceByAngle(direction: Vector3i) =
    if (!isVertical(direction)) 2 else 4

private fun nextConnectionOffset(dice: Dice, grid: MapGrid, position: Vector3i): Vector3i? {
  val availableOffsets = horizontalOrthogonalOffsets.map { (x, y) -> Vector3i(x, y, 0) }
//      horizontalDiagonalOffsets
//      .flatMap { (x, y) -> verticalOffsets.map { z -> Vector3i(x, y, z) } }
//      .plus(horizontalOrthogonalOffsets.map { (x, y) -> Vector3i(x, y, 0) })
//      .plus(upAndDown)

  val options = availableOffsets
      .filter { direction ->
        !grid.cells.containsKey(position + direction)
      }
  return if (options.any())
    dice.takeOne(options)
  else
    null
}

private val upperCell = Cell(attributes = setOf(NodeAttribute.upperLayer))

private val stairAttributes = listOf(
    NodeAttribute.stairBottom,
    NodeAttribute.stairTop
)

//private fun newVerticalPathStep(position: Vector3i, direction: Vector3i, attributes: Set<NodeAttribute> = setOf()): (MapGrid) -> MapGrid = { grid ->
//  val distance = distanceByAngle(direction)
//  val nextPosition = position + direction * distance
//
//  assert(!grid.cells.containsKey(nextPosition))
//
//  val relativeStairAttributes = if (direction.z > 0)
//    stairAttributes
//  else
//    stairAttributes.reversed()
//
//  val startCell = grid.cells[position]!!
//  grid.copy(
//      cells = grid.cells
//          .plus((0 until distance - 1).map { Pair(position + direction, upperCell) })
//          .plus(position to startCell.copy(attributes = startCell.attributes.plus(relativeStairAttributes[0])))
//          .plus(nextPosition to Cell(attributes = attributes.plus(setOf(NodeAttribute.room, relativeStairAttributes[1]))))
//          .plus(nextPosition + upVector to upperCell),
//      connections = grid.connections.plus(listOf(
//          Pair(position, nextPosition)
//      ))
//  )
//}

private fun newPathStep(position: Vector3i, direction: Vector3i, attributes: Set<NodeAttribute> = setOf()): (MapGrid) -> MapGrid = { grid ->
  //  if (isVertical(direction)) {
//    newVerticalPathStep(position, direction, attributes)(grid)
//  } else {
//    val isSlope = direction.z != 0
//    val (tunnelPosition, nextPosition) = if (isSlope) {
//      // SLopes are not straight diagonal but a flatter stair-step.
//      // (One horizontal step and one diagonal step.)
//      val horizontalDirection = direction.copy(z = 0)
//      Pair(
//          position + horizontalDirection,
//          position + horizontalDirection * distanceByAngle(direction) + Vector3i(0, 0, direction.z)
//      )
//    } else
//      Pair(position + direction, position + direction * distanceByAngle(direction))
  val nextPosition = position + direction
  assert(!grid.cells.containsKey(nextPosition))
  grid.copy(
      cells = grid.cells.plus(listOf(
//          tunnelPosition to Cell(attributes = setOf(NodeAttribute.tunnel)),
          nextPosition to Cell(attributes = attributes.plus(setOf(NodeAttribute.room)))
      )),
      connections = grid.connections.plus(listOf(
          Pair(position, nextPosition)
      ))
  )
}

private fun addPathStep(maxSteps: Int, dice: Dice, grid: MapGrid, position: Vector3i, stepCount: Int = 0): MapGrid {
  if (stepCount == maxSteps)
    return grid

  val direction = nextConnectionOffset(dice, grid, position)
  if (direction == null)
    return grid

  val attributes = if (stepCount == maxSteps - 1)
    setOf(NodeAttribute.exit)
  else
    setOf()

  val newGrid = newPathStep(position, direction, attributes)(grid)
  val nextPosition = position + direction
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

fun newWindingPathTestStartingWithStair(dice: Dice, length: Int): MapGrid {
  val startPosition = Vector3i(0, 0, 1)
  val nextPosition = Vector3i(0, 0, 5)
  val grid = newPathStep(startPosition, Vector3i(0, 0, 1))(MapGrid(
      cells = mapOf(
          startPosition to Cell(attributes = setOf(NodeAttribute.room, NodeAttribute.home))
      )
  ))
  return addPathStep(length - 1, dice, grid, nextPosition)
}
