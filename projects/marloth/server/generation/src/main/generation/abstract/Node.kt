package generation.abstract

import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector3m
import scenery.Textures
import simulation.*

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
  fun contains(id: Id) = first == id || second == id
  fun contains(node: Node) = contains(node.id)

  fun getOther(node: Node) = if (node.id == first) second else first

  fun getOther(graph: Graph, node: Node) = graph.node(getOther(node))!!

  fun nodes(graph: Graph): List<Node> = listOf(graph.node(first)!!, graph.node(second)!!)
}

typealias Connections = List<Connection>

class Node(
    override val id: Id,
    val position: Vector3m,
    val radius: Float,
    val isSolid: Boolean,
    val isWalkable: Boolean = false,
    val height: Float = 4f
) : EntityLike {
  //  val connections: MutableList<Connection> = mutableListOf()
  fun connections(graph: Graph): List<Connection> =
      graph.connections.filter { it.contains(id) }

  val floors: MutableList<FlexibleFace> = mutableListOf()
  val ceilings: MutableList<FlexibleFace> = mutableListOf()
  val walls: MutableList<FlexibleFace> = mutableListOf()

  fun neighbors(graph: Graph): Sequence<Node> = connections(graph).asSequence().mapNotNull { graph.node(it.getOther(this)) }

  fun getConnection(graph: Graph, other: Node) = graph.connections.firstOrNull { it.contains(this) && it.contains(other) }

  fun isConnected(graph: Graph, other: Node) = getConnection(graph, other) != null

  val faces: List<FlexibleFace>
    get() = floors.plus(walls).plus(ceilings)
}

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

data class Realm(
    val boundary: WorldBoundary,
    val graph: Graph,
    val mesh: FlexibleMesh,
    val nextId: IdSource
) {

  val nodes: List<Node>
    get() = graph.nodes
}

data class FaceInfo(
    var type: FaceType,
    val firstNode: Node?,
    var secondNode: Node?,
    var texture: Textures? = null,
    var debugInfo: String? = null
)

fun getFaceInfo(face: FlexibleFace): FaceInfo = (face.data as FaceInfo?)!!

fun faceNodes(info: FaceInfo) =
    listOf(info.firstNode, info.secondNode)

fun getOtherNode(node: Node, face: FlexibleFace): Node? {
  val info = getFaceInfo(face)
  return if (info.firstNode == node)
    info.secondNode
  else
    info.firstNode
}

fun getNullableFaceInfo(face: FlexibleFace): FaceInfo? = face.data as FaceInfo?

fun initializeFaceInfo(type: FaceType, node: Node, face: FlexibleFace) {
  val info = getNullableFaceInfo(face)
  face.data =
      if (info == null) {
        FaceInfo(type, node, null, null)
      } else {
        if (info.firstNode == node || info.secondNode == node)
          face.data
        else {
//          assert(info.firstNode != null && info.secondNode != null)

          FaceInfo(type, info.firstNode, node)
        }
      }
}

fun initializeNodeFaceInfo(node: Node) {
  for (face in node.walls) {
    initializeFaceInfo(FaceType.wall, node, face)
  }
  for (face in node.floors) {
    initializeFaceInfo(FaceType.floor, node, face)
  }
  for (face in node.ceilings) {
    initializeFaceInfo(FaceType.ceiling, node, face)
  }
}

fun initializeFaceInfo(realm: Realm) {
  for (node in realm.nodes) {
    initializeNodeFaceInfo(node)
  }
}

