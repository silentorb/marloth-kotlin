package generation.general

import silentorb.mythic.spatial.Vector3i
import simulation.misc.Cell
import simulation.misc.MapGrid

typealias BlockGrid = Map<Vector3i, Block>

fun mapGridFromBlockGrid(blockGrid: BlockGrid): MapGrid {
  val directions = setOf(Direction.down, Direction.east, Direction.south)
  return MapGrid(
      cells = blockGrid.mapValues { (_, block) ->
        Cell(
            attributes = block.attributes,
            slots = block.slots
        )
      },
      connections = blockGrid.keys
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
  )
}
