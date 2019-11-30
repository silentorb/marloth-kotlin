package simulation.misc

import mythic.ent.Id
import mythic.ent.firstFloatSortedBy
import mythic.sculpting.ImmutableEdgeReference
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import mythic.spatial.getCenter
import randomly.Dice

data class WorldBoundary(
    val start: Vector3,
    val end: Vector3,
    val padding: Float = 5f
) {
  val dimensions: Vector3
    get() = end - start
}

fun createWorldBoundary(length: Float): WorldBoundary {
  val half = length / 2f
  return WorldBoundary(
      Vector3(-half, -half, -half * 0.75f),
      Vector3(half, half, half * 0.75f)
  )
}

data class WorldInput(
    val boundary: WorldBoundary,
    val dice: Dice
)

typealias NodeTable = Map<Id, Node>
typealias CellPositionMap = Map<Id, Vector3i>
typealias CellBiomeMap = Map<Vector3i, BiomeName>

data class Realm(
    val graph: Graph,
    val cellMap: CellPositionMap,
    val cellBiomes: CellBiomeMap,
    val nodeList: List<Node>,
    val grid: MapGrid
) {

  val nodeTable: NodeTable = nodeList.associate { Pair(it.id, it) }
}

fun getRooms(realm: Realm): List<Node> =
    realm.nodeList.filter { it.isRoom }

// This function does not work with walls containing extreme floor slopes
val isVerticalEdgeLimited = { edge: ImmutableEdgeReference ->
  val horizontal = Vector3(edge.first.x - edge.second.x, edge.first.y - edge.second.y, 0f).length()
  val vertical = Math.abs(edge.first.z - edge.second.z)
  vertical > horizontal
}

// This function does not work with walls containing extreme floor slopes
val isHorizontalEdgeLimited = { edge: ImmutableEdgeReference -> !isVerticalEdgeLimited(edge) }

fun getFloor(face: ImmutableFace): ImmutableEdgeReference {
  val horizontalEdges = face.edges
      .filter(isHorizontalEdgeLimited)

  return if (horizontalEdges.any()) {
    horizontalEdges
        .firstFloatSortedBy { it.first.z + it.second.z }
  } else {
    val center = getCenter(face.vertices)
    face.edges
        .firstFloatSortedBy { (it.middle - center).normalize().z }
  }
}
