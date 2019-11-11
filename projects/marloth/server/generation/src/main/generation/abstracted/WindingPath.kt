package generation.abstracted

import generation.architecture.definition.BlockDefinitions
import generation.elements.Block
import generation.elements.Workbench
import generation.elements.allDirections
import generation.elements.matchBlock
import mythic.spatial.Vector3i
import randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute

private fun nextConnectionOffset(dice: Dice, grid: MapGrid, position: Vector3i): Vector3i? {
  val availableOffsets = allDirections.values
  val options = availableOffsets
      .filter { direction -> !grid.cells.containsKey(position + direction) }

  return if (options.any())
    dice.takeOne(options)
  else
    null
}

private fun newPathStep(position: Vector3i, direction: Vector3i, block: Block,
                        attributes: Set<NodeAttribute> = setOf()): (Workbench) -> Workbench = { workbench ->
  val nextPosition = position + direction
  val grid = workbench.mapGrid
  val blockGrid = workbench.blockGrid

  assert(!grid.cells.containsKey(nextPosition))

  workbench.copy(
      blockGrid = blockGrid.plus(
          nextPosition to block
      ),
      mapGrid = grid.copy(
          cells = grid.cells.plus(listOf(
              nextPosition to Cell(attributes = attributes.plus(setOf(NodeAttribute.room)))
          )),
          connections = grid.connections.plus(listOf(
              Pair(position, nextPosition)
          ))
      )
  )
}

private fun addPathStep(maxSteps: Int, dice: Dice, blocks: Set<Block>, workbench: Workbench, position: Vector3i, stepCount: Int = 0): Workbench {
  val grid = workbench.mapGrid
  if (stepCount == maxSteps)
    return workbench

  val direction = nextConnectionOffset(dice, grid, position)
  if (direction == null)
    return workbench

  val attributes = if (stepCount == maxSteps - 1)
    setOf(NodeAttribute.exit)
  else
    setOf()

  val nextPosition = position + direction
  val block = matchBlock(dice, blocks, workbench, nextPosition)
  if (block == null) {
    throw Error("Could not find a matching block")
  }
  val newWorkbench = newPathStep(position, direction, block, attributes)(workbench)
  return addPathStep(maxSteps, dice, blocks, newWorkbench, nextPosition, stepCount + 1)
}

fun newWindingPath(dice: Dice, blocks: Set<Block>, length: Int, firstBlock: Block): Workbench {
  val startPosition = Vector3i(0, 0, 0)
  val workbench = Workbench(
      blockGrid = mapOf(
          startPosition to firstBlock
      ),
      mapGrid = MapGrid(
          cells = mapOf(
              startPosition to Cell(attributes = setOf(NodeAttribute.room, NodeAttribute.home))
          )
      )
  )
  return addPathStep(length - 1, dice, blocks, workbench, startPosition)
}

fun newWindingPathTestStartingWithStair(dice: Dice, blocks: Set<Block>, length: Int): Workbench {
  val startPosition = Vector3i(0, 0, 0)
  val nextPosition = Vector3i(0, 0, 1)
  val workbench = newPathStep(startPosition, nextPosition, BlockDefinitions.stairBottom)(Workbench(
      blockGrid = mapOf(
          startPosition to BlockDefinitions.stairTop
      ),
      mapGrid = MapGrid(
          cells = mapOf(
              startPosition to Cell(attributes = setOf(NodeAttribute.room, NodeAttribute.home))
          )
      )))
  return addPathStep(length - 1, dice, blocks, workbench, nextPosition)
}
