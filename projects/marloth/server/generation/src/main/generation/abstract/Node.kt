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
    val first: Node,
    val second: Node,
    val type: ConnectionType
) {

  fun getOther(node: Node) = if (node === first) second else first

  val nodes: List<Node>
    get() = listOf(first, second)
}

class Node(
    override val id: Id,
    var position: Vector3m,
    var radius: Float,
    val isSolid: Boolean,
    val isWalkable: Boolean = false,
    var height: Float = 4f
): EntityLike {
  val connections: MutableList<Connection> = mutableListOf()
  val floors: MutableList<FlexibleFace> = mutableListOf()
  val ceilings: MutableList<FlexibleFace> = mutableListOf()
  val walls: MutableList<FlexibleFace> = mutableListOf()

  val neighbors get() = connections.asSequence().map { it.getOther(this) }

  fun getConnection(other: Node) = connections.firstOrNull { it.first === other || it.second === other }

  fun isConnected(other: Node) = getConnection(other) != null

  val faces: List<FlexibleFace>
    get() = floors.plus(walls).plus(ceilings)
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
    connections.remove(connection)
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
class Realm(val boundary: WorldBoundary) {
  val graph = NodeGraph()
  val mesh = FlexibleMesh()
  val nextId = newIdSource(1)

  val nodes: MutableList<Node>
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

