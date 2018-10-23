package simulation

import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3

enum class ConnectionType {
  tunnel,
  obstacle,
  union,
  ceilingFloor
}

class Connection(
    val first: Id,
    val second: Id,
    val type: ConnectionType
) {
  init {
    assert(first != second)
  }

  fun contains(id: Id) = first == id || second == id
  fun contains(node: Node) = contains(node.id)

  fun getOther(node: Node) = if (node.id == first) second else first

  fun getOther(graph: Graph, node: Node) = graph.node(getOther(node))!!

  fun nodes(graph: Graph): List<Node> = listOf(graph.node(first)!!, graph.node(second)!!)
}

typealias Connections = List<Connection>

data class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val height: Float,
    val isWalkable: Boolean,
    val biome: Biome,
    val isSolid: Boolean,
    val floors: MutableList<ImmutableFace>,
    val ceilings: MutableList<ImmutableFace>,
    val walls: MutableList<ImmutableFace>
) : Entity {
  val horizontalNeighbors get() = walls.asSequence().mapNotNull { getOtherNode(this, it) }
  val neighbors get() = walls.asSequence().mapNotNull { getOtherNode(this, it) }

  val faces: List<ImmutableFace>
    get() = floors.plus(walls).plus(ceilings)

  fun connections(graph: Graph): List<Connection> =
      graph.connections.filter { it.contains(id) }

  fun neighbors(graph: Graph): Sequence<Node> = connections(graph).asSequence().mapNotNull { graph.node(it.getOther(this)) }

  fun getConnection(graph: Graph, other: Node) = graph.connections.firstOrNull { it.contains(this) && it.contains(other) }

  fun isConnected(graph: Graph, other: Node) = getConnection(graph, other) != null

}

fun getPathNeighbors(node: Node) =
    node.neighbors.filter { it.isWalkable }

data class Graph(
    val nodes: List<Node>,
    val connections: Connections
) {
  fun node(id: Id): Node? = nodes.first { it.id == id }

  fun plus(graph: Graph) =
      Graph(
          nodes = nodes.plus(graph.nodes),
          connections = connections.plus(graph.connections)
      )

  fun plusConnections(newConnections: Connections) =
      copy(
          connections = connections.plus(newConnections)
      )

  fun minusConnections(oldConnections: Connections) =
      copy(
          connections = connections.minus(oldConnections)
      )

  fun plusNodes(newNodes: List<Node>) =
      copy(
          nodes = nodes.plus(newNodes)
      )

}
