package generation.general

import silentorb.mythic.spatial.Vector3i
import simulation.misc.Cell
import simulation.misc.MapGrid

data class GridCell(
    val cell: BlockCell,
    val offset: Vector3i,
    val source: Block,
)

typealias BlockGrid = Map<Vector3i, GridCell>

fun mapGridFromBlockGrid(blockGrid: BlockGrid): MapGrid {
  val directions = setOf(Direction.down, Direction.east, Direction.south)
  val cells = blockGrid.mapValues { (_, block) ->
    Cell(
        attributes = block.cell.attributes,
    )
  }

  val connections = blockGrid.keys
      .flatMap { position ->
        directions.mapNotNull { direction ->
          val otherPosition = position + directionVectors[direction]!!
          if (blockGrid.containsKey(otherPosition))
            position to otherPosition
          else
            null
        }
      }
      .toSet()

  assert(connections.any())

  return MapGrid(
      cells = cells,
      connections = connections,
  )
}
