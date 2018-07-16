package simulation

import mythic.sculpting.FlexibleFace
import mythic.sculpting.FlexibleMesh
import mythic.spatial.Vector3
import org.joml.minus
import randomly.Dice
import scenery.Textures

data class WorldBoundary(
    val start: Vector3,
    val end: Vector3,
    val padding: Float = 5f
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

data class WorldInput(
    val boundary: WorldBoundary,
    val dice: Dice,
    val biomes: List<Biome>
)

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

enum class FaceType {
  ceiling,
  floor,
  space,
  wall
}

data class FaceInfo(
    var type: FaceType,
    val firstNode: Node?,
    var secondNode: Node?,
    var texture: Textures? = null,
    var debugInfo: String? = null
)

fun faceNodes(info: FaceInfo) =
    listOf(info.firstNode, info.secondNode)

fun getOtherNode(info: FaceInfo, node: Node) =
    if (info.firstNode == node)
      info.secondNode
    else
      info.firstNode

// May be faster to cast straight to non-nullable but at least for debugging
// it may be better to explicitly non-null cast.
fun getFaceInfo(face: FlexibleFace): FaceInfo = (face.data as FaceInfo?)!!

val isWall = { face: FlexibleFace -> getFaceInfo(face).type == FaceType.wall }
val isSpace = { face: FlexibleFace -> getFaceInfo(face).type == FaceType.space }

fun getNullableFaceInfo(face: FlexibleFace): FaceInfo? = face.data as FaceInfo?

class AbstractWorld(val boundary: WorldBoundary) {
  val graph = NodeGraph()
  val mesh = FlexibleMesh()
//  val faceMap: FaceSectorMap = mutableMapOf()

  val nodes: MutableList<Node>
    get() = graph.nodes

  val locationNodes: List<Node>
    get() = nodes.filter { it.isWalkable }

  val connections: MutableList<Connection>
    get() = graph.connections

  val floors: List<FlexibleFace>
    get() = nodes.flatMap { it.floors }.distinct()

  val walls: List<FlexibleFace>
    get() = nodes.flatMap { it.walls }.distinct()
}

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

fun initializeFaceInfo(abstractWorld: AbstractWorld) {
  for (node in abstractWorld.nodes) {
    initializeNodeFaceInfo(node)
  }
}

fun getOtherNode(node: Node, face: FlexibleFace): Node? {
  val info = getFaceInfo(face)
  return if (info.firstNode == node)
    info.secondNode
  else
    info.firstNode
}

fun getFloor(face: FlexibleFace) =
    face.edges.filter { it.first.z == it.second.z }
        .sortedBy { it.first.z }
        .first()