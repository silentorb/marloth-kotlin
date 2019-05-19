package simulation

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.firstSortedBy
import mythic.sculpting.ImmutableEdgeReference
import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector3
import mythic.spatial.getCenter
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
      Vector3(-half, -half, -half * 0.75f),
      Vector3(half, half, half * 0.75f)
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

// May be faster to cast straight to non-nullable but at least for debugging
// it may be better to explicitly non-null cast.
fun getFaceInfo(faces: ConnectionTable, face: ImmutableFace): ConnectionFace = faces[face.id]!!

val isSolidWall = { face: ConnectionFace ->
  face.faceType == FaceType.wall && face.texture != null
}
val isSpace = { face: ConnectionFace -> face.faceType == FaceType.space }

private fun debugId(id: Id): Id {
  if (id == 41L) {
    val k = 0
  }
  return id
}

data class ConnectionFace(
    override val id: Id,
    var faceType: FaceType,
    val firstNode: Id,
    val secondNode: Id,
    var texture: Textures? = null,
    var debugInfo: String? = null
) : Entity {
  val nodes: List<Id> = listOf(firstNode, secondNode).minus(voidNodeId)
  val _deleteme = if (id == 41L)
    1
  else
    0

  fun contains(node: Id) = nodes.contains(node)

  fun other(id: Id): Id? =
      if (firstNode == id)
        secondNode
      else if (id == secondNode)
        firstNode
      else
        null

}

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
    val mesh: ImmutableMesh,
    val doorFrameNodes: List<Id>
) {

  val nodeTable: NodeTable = nodeList.associate { Pair(it.id, it) }
  val nodeFaces: OneToManyMap = mapNodeFaces(nodeTable, faces)

  val locationNodes: List<Node>
    get() = nodeList.filter { it.isWalkable }

  val floors: List<ImmutableFace>
    get() = nodeList.flatMap { it.floors }.distinct().map { mesh.faces[it]!! }

  val walls: List<ImmutableFace>
    get() = nodeList.flatMap { it.walls }.distinct().map { mesh.faces[it]!! }
}

fun getFaces(faces: FaceList, node: Node) =
    faces.filter { it.firstNode == node.id || it.secondNode == node.id }

fun getFloors(faces: FaceList, node: Node) =
    getFaces(faces, node).filter { it.faceType == FaceType.floor }

fun initializeFaceInfo(faces: ConnectionTable, type: FaceType, node: Node, id: Id): ConnectionFace {
  val info = faces[id]
  return if (info == null) {
    ConnectionFace(id, type, node.id, voidNodeId, null)
  } else {
    if (info.firstNode == node.id || info.secondNode == node.id)
      info
    else {
      ConnectionFace(id, type, info.firstNode, node.id)
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

fun getOtherNode(id: Id, info: ConnectionFace): Id? {
  return if (info.firstNode == id)
    info.secondNode
  else
    info.firstNode
}

fun getNode(realm: Realm, id: Id) =
    realm.nodeList.firstOrNull { it.id == id }

// This function does not work with walls containing extreme floor slopes
val isVerticalEdgeLimited = { edge: ImmutableEdgeReference ->
  val horizontal = Vector3(edge.first.x - edge.second.x, edge.first.y - edge.second.y, 0f).length()
  val vertical = Math.abs(edge.first.z - edge.second.z)
  vertical > horizontal
}

// This function does not work with walls containing extreme floor slopes
val isHorizontalEdgeLimited = { edge: ImmutableEdgeReference -> !isVerticalEdgeLimited(edge) }

fun getFloor(face: ImmutableFace): ImmutableEdgeReference {
  val horizontalEdges = face.edges
      .filter(isHorizontalEdgeLimited)

  return if (horizontalEdges.any()) {
    horizontalEdges
        .firstSortedBy { it.first.z + it.second.z }
  } else {
    val center = getCenter(face.vertices)
    face.edges
        .firstSortedBy { (it.middle - center).normalize().z }
  }
}

fun getCeiling(face: ImmutableFace): ImmutableEdgeReference {
  val horizontalEdges = face.edges
      .filter(isHorizontalEdgeLimited)

  return if (horizontalEdges.any()) {
    horizontalEdges
        .firstSortedBy { -(it.first.z + it.second.z) }
  } else {
    val center = getCenter(face.vertices)
    face.edges
        .firstSortedBy { -(it.middle - center).normalize().z }
  }
}
