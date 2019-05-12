package generation.abstracted

import mythic.ent.Id
import scenery.Textures
import simulation.FaceType
import simulation.Node
import simulation.NodeTable

enum class ConnectionType {
  tunnel,
  obstacle,
  union,
  ceilingFloor,
  vertical
}

data class InitialConnection(
    val first: Id,
    val second: Id,
    val type: ConnectionType,
    val faceType: FaceType,
    var texture: Textures? = null,
    var debugInfo: String? = null
) {
  init {
    assert(first != second)
  }

  fun contains(id: Id) = first == id || second == id
  fun contains(node: Node) = contains(node.id)

  fun getOther(node: Node) = if (node.id == first) second else first
  fun getOther(node: Id) = if (node == first) second else first

  fun getOther(graph: Graph, node: Node) = graph.node(getOther(node))!!

  fun nodes(graph: Graph): List<Node> = listOf(graph.node(first)!!, graph.node(second)!!)

  val nodes: List<Id> = listOf(first, second)

}typealias InitialConnections = List<InitialConnection>

data class Graph(
    val nodes: NodeTable,
    val connections: InitialConnections,
    val tunnels: List<Id> = listOf(),
    val doorways: List<Id> = listOf()
) {
  fun node(id: Id): Node? = nodes[id]!!

  fun plus(graph: Graph) =
      Graph(
          nodes = nodes.plus(graph.nodes),
          connections = connections.plus(graph.connections),
          tunnels = tunnels.plus(graph.tunnels)
      )

  fun minusConnections(oldConnections: InitialConnections) =
      copy(
          connections = connections.minus(oldConnections)
      )

}

fun tunnelLength(graph: Graph, connection: InitialConnection): Float {
  val first = graph.nodes[connection.first]!!
  val second = graph.nodes[connection.second]!!
  return first.position.distance(second.position) - first.radius - second.radius
}
