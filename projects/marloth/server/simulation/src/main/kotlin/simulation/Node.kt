package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3
import scenery.Textures

enum class ConnectionType {
  tunnel,
  obstacle,
  union,
  ceilingFloor
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

  fun getOther(graph: Graph, node: Node) = graph.node(getOther(node))!!

  fun nodes(graph: Graph): List<Node> = listOf(graph.node(first)!!, graph.node(second)!!)
}

typealias InitialConnections = List<InitialConnection>

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

  val faces: List<ImmutableFace>
    get() = floors.plus(walls).plus(ceilings)

  fun connections(graph: Graph): List<InitialConnection> =
      graph.connections.filter { it.contains(id) }

  fun neighbors(graph: Graph): Sequence<Node> = connections(graph).asSequence().mapNotNull { graph.node(it.getOther(this)) }

  fun getConnection(graph: Graph, other: Node) = graph.connections.firstOrNull { it.contains(this) && it.contains(other) }

  fun isConnected(graph: Graph, other: Node) = getConnection(graph, other) != null
}

fun horizontalNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node, faces[it.id]!!) }

fun nodeNeighbors(faces: ConnectionTable, node: Node) = node.walls.asSequence().mapNotNull { getOtherNode(node, faces[it.id]!!) }

fun getPathNeighbors(nodes: NodeTable, faces: ConnectionTable, node: Node) =
    nodeNeighbors(faces, node)
        .map { nodes[it]!! }
        .filter { it.isWalkable }

data class Graph(
    val nodes: List<Node>,
    val connections: InitialConnections
) {
  fun node(id: Id): Node? = nodes.first { it.id == id }

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
