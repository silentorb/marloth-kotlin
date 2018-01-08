package generation

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

class Node(var position: Vector3, var radius: Float) {
  val connections: MutableList<Connection> = mutableListOf()

  fun getNeighbors() = connections.map { it.getOther(this) }

  fun getConnection(other: Node) = connections.firstOrNull { it.second === other }

  fun isConnected(other: Node) = getConnection(other) != null
}