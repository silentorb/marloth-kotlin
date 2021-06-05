package generation.general

import silentorb.mythic.spatial.Vector3i

data class CellDirection(
    val cell: Vector3i,
    val direction: Direction
)

typealias GridSideMap = Map<CellDirection, Side>

fun getUsableCellSide(gridSideMap: GridSideMap): UsableConnectionTypes = { position ->
  { direction ->
    gridSideMap[CellDirection(position, direction)]!!
  }
}
