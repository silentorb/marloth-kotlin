package generation.abstracted

import generation.architecture.definition.BlockDefinitions
import generation.elements.*
import mythic.spatial.Vector3i
import randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute
import simulation.misc.cellConnections

private fun nextDirection(dice: Dice, config: BlockConfig, workbench: Workbench, position: Vector3i): Vector3i? {
  val grid = workbench.mapGrid
  val blockGrid = workbench.blockGrid
  val block = blockGrid[position]!!
  val availableOffsets = allDirections
  val options = availableOffsets
      .filter { direction -> !grid.cells.containsKey(position + direction.value) }

  val essential = options.filter { direction ->
    val side = block.sides[direction.key]!!
    side.none { config.independentConnections.contains(it) }
  }

  val finalOptions = (if (essential.any()) essential else options)
      .map { it.value }

  return if (finalOptions.any())
    dice.takeOne(finalOptions)
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
              nextPosition to Cell(attributes = attributes.plus(setOf(NodeAttribute.fullFloor)))
          )),
          connections = grid.connections.plus(listOf(
              Pair(position, nextPosition)
          ))
      )
  )
}

private fun addPathStep(maxSteps: Int, dice: Dice, config: BlockConfig, workbench: Workbench, position: Vector3i, stepCount: Int = 0): Workbench {
  val grid = workbench.mapGrid
  if (stepCount == maxSteps)
    return workbench

  val direction = nextDirection(dice,config, workbench, position)
  if (direction == null)
    return workbench

  val attributes = if (stepCount == maxSteps - 1)
    setOf(NodeAttribute.exit)
  else
    setOf()

  val blocks = config.blocks
  val nextPosition = position + direction
  val block = matchBlock(dice, blocks, workbench, nextPosition)
  if (block == null) {
    val relevantConnections = cellConnections(grid.connections, position)
    throw Error("Could not find a matching block")
  }
  val newWorkbench = newPathStep(position, direction, block, attributes)(workbench)
  return addPathStep(maxSteps, dice, config, newWorkbench, nextPosition, stepCount + 1)
}

fun newWindingWorkbench(firstBlock: Block): Workbench {
  val startPosition = Vector3i.zero
  return Workbench(
      blockGrid = mapOf(
          startPosition to firstBlock
      ),
      mapGrid = MapGrid(
          cells = mapOf(
              startPosition to Cell(attributes = setOf(NodeAttribute.fullFloor, NodeAttribute.home))
          )
      )
  )
}

fun newWindingPath(dice: Dice, config: BlockConfig, length: Int, firstBlock: Block): Workbench {
  val startPosition = Vector3i.zero
  val workbench = newWindingWorkbench(firstBlock)
  return addPathStep(length - 1, dice, config, workbench, startPosition)
}

fun newWindingPathTestStartingWithStair(dice: Dice, config: BlockConfig, length: Int): Workbench {
  val startPosition = Vector3i.zero
  val nextPosition = Vector3i(0, 0, 1)
  val workbench = newPathStep(startPosition, nextPosition, BlockDefinitions.stairBottom)(Workbench(
      blockGrid = mapOf(
          startPosition to BlockDefinitions.stairTop
      ),
      mapGrid = MapGrid(
          cells = mapOf(
              startPosition to Cell(attributes = setOf(NodeAttribute.fullFloor, NodeAttribute.home))
          )
      )))
  return addPathStep(length - 1, dice, config, workbench, nextPosition)
}
