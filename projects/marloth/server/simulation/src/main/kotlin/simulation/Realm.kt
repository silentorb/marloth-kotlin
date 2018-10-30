package simulation

import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector3
import physics.voidNode
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

//data class NodeFace(
//    var faceType: FaceType,
//    val firstNode: Node?,
//    var secondNode: Node?,
//    var texture: Textures? = null,
//    var debugInfo: String? = null
//)

//fun faceNodes(info: NodeFace) =
//    listOf(info.firstNode, info.secondNode)

// May be faster to cast straight to non-nullable but at least for debugging
// it may be better to explicitly non-null cast.
fun getFaceInfo(faces: FaceTable, face: ImmutableFace): NodeFace = faces[face.id]!!

val isSolidWall = { face: NodeFace ->
  face.faceType == FaceType.wall && face.texture != null
}
val isSpace = { face: NodeFace -> face.faceType == FaceType.space }

//fun getNullableFaceInfo(face: ImmutableFace): NodeFace? = face.data as NodeFace?

/*
class NodeEdge(
    val first: Vector3,
    val second: Vector3,
    val faces: List<Id>
) {
  val vertices = listOf(first, second)

  val middle: Vector3
    get() = (first + second) * 0.5f

  fun getReference(face: NodeFace) = face.edges.first { it.edge == this }

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
data class NodeFace(
    override val id: Id,
    var faceType: FaceType,
    val firstNode: Id,
    var secondNode: Id,
    var texture: Textures? = null,
    var debugInfo: String? = null
) : Entity

typealias FaceTable = Map<Id, NodeFace>
typealias FaceList = Collection<NodeFace>

typealias NodeTable = Map<Id, Node>

data class RealmMesh(
    val faces: FaceTable
)

data class Realm(
    val boundary: WorldBoundary,
    val nodeList: List<Node>,
    val faces: FaceTable,
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

fun initializeFaceInfo(faces: FaceTable, type: FaceType, node: Node, face: ImmutableFace): NodeFace {
  val info = faces[face.id]
  return if (info == null) {
    NodeFace(face.id, type, node.id, voidNodeId, null)
  } else {
    if (info.firstNode == node.id || info.secondNode == node.id)
      info
    else {
      NodeFace(face.id, type, info.firstNode, node.id)
    }
  }
}

fun initializeNodeFaceInfo(faces: FaceTable, node: Node): List<NodeFace> {
  return node.walls.map { initializeFaceInfo(faces, FaceType.wall, node, it) }
      .plus(node.floors.map { initializeFaceInfo(faces, FaceType.floor, node, it) })
      .plus(node.ceilings.map { initializeFaceInfo(faces, FaceType.ceiling, node, it) })
}

fun initializeFaceInfo(nodes: List<Node>, faces: FaceTable) {
  for (node in nodes) {
    initializeNodeFaceInfo(faces, node)
  }
}

fun getOtherNode(node: Node, info: NodeFace): Id? {
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