package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector3
import physics.voidNodeId
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
    val dice: Dice
)

enum class FaceType {
  ceiling,
  floor,
  space,
  wall
}

//data class ConnectionFace(
//    var faceType: FaceType,
//    val firstNode: Node?,
//    var secondNode: Node?,
//    var texture: Textures? = null,
//    var debugInfo: String? = null
//)

//fun faceNodes(info: ConnectionFace) =
//    listOf(info.firstNode, info.secondNode)

// May be faster to cast straight to non-nullable but at least for debugging
// it may be better to explicitly non-null cast.
fun getFaceInfo(faces: ConnectionTable, face: ImmutableFace): ConnectionFace = faces[face.id]!!

val isSolidWall = { face: ConnectionFace ->
  face.faceType == FaceType.wall && face.texture != null
}
val isSpace = { face: ConnectionFace -> face.faceType == FaceType.space }

//fun getNullableFaceInfo(face: ImmutableFace): ConnectionFace? = face.data as ConnectionFace?

/*
class NodeEdge(
    val first: Vector3,
    val second: Vector3,
    val faces: List<Id>
) {
  val vertices = listOf(first, second)

  val middle: Vector3
    get() = (first + second) * 0.5f

  fun getReference(face: ConnectionFace) = face.edges.first { it.edge == this }

  val references: List<NodeEdgeReference>
    get() = faces.map { getReference(it) }

  fun matches(a: Vector3, b: Vector3): Boolean =
      (first == a && second == b)
          || (first == b && second == a)
}

class NodeEdgeReference(
    val edge: NodeEdge,
    var next: NodeEdgeReference?,
    var previous: NodeEdgeReference?,
    var direction: Boolean
) {
  val vertices: List<Vector3>
    get() = if (direction) edge.vertices else listOf(edge.second, edge.first)

  val faces: List<Id>
    get() = edge.faces

  val first: Vector3
    get() = if (direction) edge.first else edge.second

  val second: Vector3
    get() = if (direction) edge.second else edge.first

  val otherNodeEdgeReferences: List<NodeEdgeReference>
    get() = edge.references.filter { it != this }

  val middle: Vector3
    get() = edge.middle

}
*/
data class ConnectionFace(
    override val id: Id,
    var faceType: FaceType,
    val firstNode: Id,
    val secondNode: Id,
    var texture: Textures? = null,
    var debugInfo: String? = null
) : Entity

typealias ConnectionTable = Map<Id, ConnectionFace>
typealias FaceList = Collection<ConnectionFace>

typealias NodeTable = Map<Id, Node>

data class RealmMesh(
    val faces: ConnectionTable
)

data class Realm(
    val boundary: WorldBoundary,
    val nodeList: List<Node>,
    val faces: ConnectionTable,
    val mesh: ImmutableMesh
) {

  val nodeTable: NodeTable = nodeList.associate { Pair(it.id, it) }

  val locationNodes: List<Node>
    get() = nodeList.filter { it.isWalkable }

  val floors: List<ImmutableFace>
    get() = nodeList.flatMap { it.floors }.distinct()

  val walls: List<ImmutableFace>
    get() = nodeList.flatMap { it.walls }.distinct()
}

fun getFaces(faces: FaceList, node: Node) =
    faces.filter { it.firstNode == node.id || it.secondNode == node.id }

fun getFloors(faces: FaceList, node: Node) =
    getFaces(faces, node).filter { it.faceType == FaceType.floor }

fun initializeFaceInfo(faces: ConnectionTable, type: FaceType, node: Node, face: ImmutableFace): ConnectionFace {
  val info = faces[face.id]
  return if (info == null) {
    ConnectionFace(face.id, type, node.id, voidNodeId, null)
  } else {
    if (info.firstNode == node.id || info.secondNode == node.id)
      info
    else {
      ConnectionFace(face.id, type, info.firstNode, node.id)
    }
  }
}

fun initializeNodeFaceInfo(faces: ConnectionTable, node: Node): List<ConnectionFace> {
  return node.walls.map { initializeFaceInfo(faces, FaceType.wall, node, it) }
      .plus(node.floors.map { initializeFaceInfo(faces, FaceType.floor, node, it) })
      .plus(node.ceilings.map { initializeFaceInfo(faces, FaceType.ceiling, node, it) })
}

fun initializeFaceInfo(nodes: List<Node>, faces: ConnectionTable) {
  for (node in nodes) {
    initializeNodeFaceInfo(faces, node)
  }
}

fun getOtherNode(node: Node, info: ConnectionFace): Id? {
  return if (info.firstNode == node.id)
    info.secondNode
  else
    info.firstNode
}

fun getNode(realm: Realm, id: Id) =
    realm.nodeList.firstOrNull { it.id == id }

fun getFloor(face: ImmutableFace) =
    face.edges.filter { it.first.z == it.second.z }
        .sortedBy { it.first.z }
        .first()