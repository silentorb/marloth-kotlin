package simulation

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3

enum class ConnectionType {
  tunnel,
  obstacle,
  union
}

class Connection(
    val first: Node,
    val second: Node,
    val type: ConnectionType
) {

  fun getOther(node: Node) = if (node === first) second else first

  val nodes: List<Node>
    get() = listOf(first, second)
}

class Node(
    var position: Vector3,
    var radius: Float,
    val isSolid: Boolean,
    val biome: Biome,
    val isWalkable: Boolean = false
) {
  val connections: MutableList<Connection> = mutableListOf()
  var index = 0
  val floors: MutableList<FlexibleFace> = mutableListOf()
  val ceilings: MutableList<FlexibleFace> = mutableListOf()
  val walls: MutableList<FlexibleFace> = mutableListOf()

  val neighbors get() = connections.asSequence().map { it.getOther(this) }

  fun getConnection(other: Node) = connections.firstOrNull { it.first === other || it.second === other }

  fun isConnected(other: Node) = getConnection(other) != null

  val faces: List<FlexibleFace>
    get() = floors.plus(walls).plus(ceilings)
}