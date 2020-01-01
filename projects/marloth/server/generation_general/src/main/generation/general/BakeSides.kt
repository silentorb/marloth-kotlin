package generation.general

import silentorb.mythic.spatial.Vector3i
import simulation.misc.ConnectionSet

data class CellDirection(
    val cell: Vector3i,
    val direction: Direction
)

typealias GridSideMap = Map<CellDirection, Side>

fun bakeSides(independentConnectionTypes: Set<Any>,
              openConnectionTypes: Set<Any>,
              connections: ConnectionSet,
              blockGrid: BlockGrid): GridSideMap {
  val getUsable = getUsableCellSide(independentConnectionTypes, openConnectionTypes, connections, blockGrid)

  return blockGrid.flatMap { (cell, block) ->
    block.sides.map { (direction, _) ->
      Pair(CellDirection(cell, direction), getUsable(cell)(direction))
    }
  }
      .associate { it }
}

fun getUsableCellSide(gridSideMap: GridSideMap): UsableConnectionTypes = { position ->
  { direction ->
    gridSideMap[CellDirection(position, direction)]!!
  }
}
