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

  return MapGrid(
      cells = cells,
      connections = connections,
  )
}

fun getTurnDirection(turns: Int): Direction? =
    when ((turns + 4) % 4) {
      0 -> Direction.east
      1 -> Direction.north
      2 -> Direction.west
      3 -> Direction.south
      else -> null
    }

fun rotateZ(turns: Int, value: Direction): Direction =
    getTurnDirection(horizontalDirectionList.indexOf(value) + turns) ?: value

fun rotateZ(turns: Int, value: Vector3i): Vector3i {
  val (x, y, z) = value
  return when (turns) {
    3 -> Vector3i(y, -x, z)
    2 -> Vector3i(-x, -y, z)
    1 -> Vector3i(-y, x, z)
    else -> value
  }
}
