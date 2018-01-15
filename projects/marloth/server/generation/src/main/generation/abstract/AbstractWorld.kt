package generation.abstract

import mythic.spatial.Vector3
import org.joml.minus

data class WorldBoundary(
    val start: Vector3,
    val end: Vector3
) {
  val dimensions: Vector3
    get() = end - start
}

class AbstractWorld(val boundary: WorldBoundary) {
  val nodes: MutableList<Node> = mutableListOf()
  val connections: MutableList<Connection> = mutableListOf()

  fun connect(first: Node, second: Node, type: ConnectionType): Connection {
    val connection = Connection(first, second, type)
    connections.add(connection)
    first.connections.add(connection)
    second.connections.add(connection)
    return connection
  }

  constructor(start: Vector3, end: Vector3) : this(WorldBoundary(start, end))

  fun removeNode(node: Node) {
    nodes.remove(node)
    for (connection in node.connections) {
      connection.getOther(node).connections.remove(connection)
      connections.remove(connection)
    }
    node.connections.clear()
  }
}