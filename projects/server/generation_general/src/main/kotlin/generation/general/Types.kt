package generation.general

import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i

data class GridCell(
    val cell: BlockCell,
    val offset: Vector3i,
    val source: Block,
)

typealias BlockGrid = Map<Vector3i, GridCell>

data class WorldBoundary(
    val start: Vector3,
    val end: Vector3,
    val padding: Float = 5f
) {
  val dimensions: Vector3
    get() = end - start
}
