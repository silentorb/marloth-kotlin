package simulation.misc

import silentorb.mythic.ent.Id
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector3i

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
    val cellBiomes: CellBiomeMap,
    val nodeList: List<Node>,
    val grid: MapGrid
) {

  val nodeTable: NodeTable = nodeList.associate { Pair(it.id, it) }
}

fun getRooms(realm: Realm): List<Node> =
    realm.nodeList.filter { it.isRoom }
