package simulation.misc

import mythic.ent.Id

enum class ConnectionType {
  doorway,
  union,
  vertical
}

data class InitialConnection(
    val first: Id,
    val second: Id,
    val type: ConnectionType,
    var debugInfo: String? = null
) {
  init {
    assert(first != second)
  }

  fun contains(id: Id) = first == id || second == id
  fun contains(node: Node) = contains(node.id)

  fun other(node: Node) = if (node.id == first) second else first
  fun other(node: Id) = if (node == first) second else first

  fun other(graph: Graph, node: Node) = graph.node(other(node))!!

  fun otherOrNull(node: Id): Id? =
      if (node == first)
        second
      else if (node == second)
        first
      else null

  fun nodes(graph: Graph): List<Node> = listOf(graph.node(first)!!, graph.node(second)!!)

  val nodes: List<Id> = listOf(first, second)

}typealias InitialConnections = List<InitialConnection>

data class Graph(
    val nodes: NodeTable,
    val connections: InitialConnections,
    val doorways: List<Id> = listOf()
) {
  fun node(id: Id): Node? = nodes[id]!!

  fun plus(graph: Graph) =
      Graph(
          nodes = nodes.plus(graph.nodes),
          connections = connections.plus(graph.connections)
      )

  fun minusConnections(oldConnections: InitialConnections) =
      copy(
          connections = connections.minus(oldConnections)
      )
}

fun nodeNeighbors2(faces: InitialConnections, id: Id) = faces.mapNotNull { it.otherOrNull(id) }

fun tunnelLength(graph: Graph, connection: InitialConnection): Float {
  val first = graph.nodes[connection.first]!!
  val second = graph.nodes[connection.second]!!
  return first.position.distance(second.position) - first.radius - second.radius
}
