package generation.abstracted

import generation.general.*
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.CellAttribute
import simulation.misc.cellConnections

private fun nextDirection(dice: Dice, config: BlockConfig, blockGrid: BlockGrid,
                          position: Vector3i): Map.Entry<Direction, Vector3i>? {
  val options = possibleNextDirections(config, blockGrid, position)

  return if (options.any())
    dice.takeOne(options.entries)
  else
    null
}

private fun newPathStep(position: Vector3i, direction: Vector3i, block: Block,
                        attributes: Set<CellAttribute> = setOf()): (Workbench) -> Workbench = { workbench ->
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
              nextPosition to Cell(attributes = attributes.plus(block.attributes))
          )),
          connections = grid.connections.plus(listOf(
              Pair(position, nextPosition)
          ))
      )
  )
}

tailrec fun addPathStep(maxSteps: Int, dice: Dice, config: BlockConfig, workbench: Workbench, position: Vector3i, stepCount: Int = 0): Workbench {
  val grid = workbench.mapGrid
  if (stepCount == maxSteps)
    return workbench

  val directionPair = nextDirection(dice, config, workbench.blockGrid, position)
  if (directionPair == null) {
    return workbench
  }

  val (direction, offset) = directionPair
  val attributes = if (stepCount == maxSteps - 1)
    setOf(CellAttribute.exit)
  else
    setOf()

  val nextPosition = position + offset
  val openConnections = config.openConnections
//  val blocks = if (stepCount == maxSteps - 1) {
//    val directions = allDirections.minus(oppositeDirections[direction]!!)
//    config.blocks.filter(isBlockIndependent(config.isSideIndependent, directions)).toSet()
//  } else
//    config.blocks

  val block = matchConnectingBlock(dice, config.blocks, openConnections, workbench, direction, nextPosition)
  if (block == null) {
    val relevantConnections = cellConnections(grid.connections, position)
    return workbench
//    throw Error("Could not find a matching block")
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
              startPosition to Cell(attributes = setOf(CellAttribute.fullFloor, CellAttribute.home))
          )
      )
  )
}

fun windingPath(dice: Dice, config: BlockConfig, length: Int,
                startPosition: Vector3i = Vector3i.zero): (Workbench) -> Workbench = { workbench ->
  val result = addPathStep(length - 1, dice, config, workbench, startPosition)
  assert(result.blockGrid.size > 0)
  result
}
