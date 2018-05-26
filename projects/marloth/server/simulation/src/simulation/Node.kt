package simulation

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3

enum class ConnectionType {
  tunnel,
  union
}

class Connection(
    val first: Node,
    val second: Node,
    val type: ConnectionType
) {

  fun getOther(node: Node) = if (node === first) second else first
}

enum class NodeType {
  room,
  space,
  tunnel
}

class Node(var position: Vector3, var radius: Float, val type: NodeType) {
  val connections: MutableList<Connection> = mutableListOf()
  var index = 0
  val floors: MutableList<FlexibleFace> = mutableListOf()
  val walls: MutableList<FlexibleFace> = mutableListOf()

  val neighbors get() = connections.asSequence().map { it.getOther(this) }

  fun getConnection(other: Node) = connections.firstOrNull { it.first === other || it.second === other }

  fun isConnected(other: Node) = getConnection(other) != null

  val faces: List<FlexibleFace>
    get() = floors.plus(walls)
}