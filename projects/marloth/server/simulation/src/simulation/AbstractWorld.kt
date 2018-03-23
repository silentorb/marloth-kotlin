package simulation

import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector3
import org.joml.minus
import randomly.Dice

data class WorldBoundary(
    val start: Vector3,
    val end: Vector3
) {
  val dimensions: Vector3
    get() = end - start
}

fun createWorldBoundary(length: Float): WorldBoundary {
  val half = length / 2f
  return WorldBoundary(
      Vector3(-half, -half, -half),
      Vector3(half, half, half)
  )
}

data class WorldInput(val boundary: WorldBoundary, val dice: Dice) {
}

class NodeGraph {
  val nodes: MutableList<Node> = mutableListOf()
  val connections: MutableList<Connection> = mutableListOf()

  fun connect(first: Node, second: Node, type: ConnectionType): Connection {
    val connection = Connection(first, second, type)
    connections.add(connection)
    first.connections.add(connection)
    second.connections.add(connection)
    return connection
  }

  fun disconnect(connection: Connection) {
    connection.first.connections.remove(connection)
    connection.second.connections.remove(connection)
  }

  fun removeNode(node: Node) {
    nodes.remove(node)
    for (connection in node.connections) {
      connection.getOther(node).connections.remove(connection)
      connections.remove(connection)
    }
    node.connections.clear()
  }
}

class AbstractWorld(val boundary: WorldBoundary) {
  val graph = NodeGraph()
  val mesh = FlexibleMesh()

  val nodes: MutableList<Node>
    get() = graph.nodes

  val connections: MutableList<Connection>
    get() = graph.connections

  val floors: List<FlexibleFace>
    get() = nodes.flatMap { it.floors }

  val walls: List<FlexibleFace>
    get() = nodes.flatMap { it.walls }
}