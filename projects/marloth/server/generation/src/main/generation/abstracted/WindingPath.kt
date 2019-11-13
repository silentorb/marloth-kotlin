package generation.abstracted

import generation.elements.*
import mythic.spatial.Vector3i
import randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute
import simulation.misc.cellConnections

private fun nextDirection(dice: Dice, config: BlockConfig, workbench: Workbench,
                          position: Vector3i): Map.Entry<Direction, Vector3i>? {
  val grid = workbench.mapGrid
  val blockGrid = workbench.blockGrid
  val block = blockGrid[position]!!
  val options = allDirections
      .filter { direction -> !grid.cells.containsKey(position + direction.value) }
      .filter { direction -> isSideOpen(config.openConnections, block.sides.getValue(direction.key)) }

  val essential = options.filter { direction ->
    val side = block.sides[direction.key]!!
    side.none { config.independentConnections.contains(it) }
  }

  val finalOptions = (if (essential.any()) essential else options)

  return if (finalOptions.any())
    dice.takeOne(finalOptions.entries)
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

  val directionPair = nextDirection(dice, config, workbench, position)
  if (directionPair == null)
    return workbench

  val (direction, offset) = directionPair
  val attributes = if (stepCount == maxSteps - 1)
    setOf(NodeAttribute.exit)
  else
    setOf()

  val nextPosition = position + offset
  val blocks = config.blocks
  val openConnections = config.openConnections
  val block = matchConnectingBlock(dice, blocks, openConnections, workbench, direction, nextPosition)
  if (block == null) {
    val relevantConnections = cellConnections(grid.connections, position)
    throw Error("Could not find a matching block")
  }
  val newWorkbench = newPathStep(position, offset, block, attributes)(workbench)
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
