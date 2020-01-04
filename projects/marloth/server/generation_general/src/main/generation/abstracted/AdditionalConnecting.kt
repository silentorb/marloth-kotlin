package generation.abstracted

import generation.general.*
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i
import simulation.misc.CellAttribute
import simulation.misc.ConnectionPair
import simulation.misc.containsConnection

// Connects neighboring cells that are not currently connected and have sides that support being connected

fun canConnect(blockGrid: BlockGrid, openConnectionTypes: Set<Any>, first: Vector3i, direction: Direction): Boolean {
  val firstSide = blockGrid[first]!!.sides[direction]!!
  val second = first + directionVectors[direction]!!
  val secondDirection = oppositeDirections[direction]!!
  val secondSide = blockGrid[second]!!.sides[secondDirection]!!
  return firstSide.any { openConnectionTypes.contains(it) && secondSide.contains(it) }
}

fun availableNeighoringCellPairs(blockConfig: BlockConfig, workbench: Workbench): List<ConnectionPair> {
  val blockGrid = workbench.blockGrid
  val connections = workbench.mapGrid.connections
  val openConnections = blockConfig.openConnections
  val cells = workbench.mapGrid.cells.filter { !it.value.attributes.contains(CellAttribute.home) }
  val directions = setOf(Direction.down, Direction.east, Direction.south)
  return cells.keys.flatMap { cell ->
    directions.mapNotNull { direction ->
      val neighbor = cell + directionVectors[direction]!!
      if (blockGrid.containsKey(neighbor)
          && !containsConnection(connections, cell, neighbor)
          && canConnect(blockGrid, openConnections, cell, direction)) {
        Pair(cell, neighbor)
      } else
        null
    }
  }
}

fun additionalConnecting(dice: Dice, blockConfig: BlockConfig, rate: Float): (Workbench) -> Workbench = { workbench ->
  val mapGrid = workbench.mapGrid
  val connections = workbench.mapGrid.connections
  val possibleConnections = availableNeighoringCellPairs(blockConfig, workbench)
  val amount = (possibleConnections.size.toFloat() * rate).toInt()
  val newConnections = dice.take(possibleConnections, amount)
  workbench.copy(
      mapGrid = mapGrid.copy(
          connections = connections.plus(newConnections)
      )
  )
}
