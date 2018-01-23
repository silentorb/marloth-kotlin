package simulation

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
  tunnel
}

class Node(var position: Vector3, var radius: Float, val type: NodeType) {
  val connections: MutableList<Connection> = mutableListOf()
  var index = 0
  var corners: List<Vector3> = listOf()

  fun getNeighbors() = connections.asSequence().map { it.getOther(this) }

  fun getConnection(other: Node) = connections.firstOrNull { it.first === other || it.second === other }

  fun isConnected(other: Node) = getConnection(other) != null
}