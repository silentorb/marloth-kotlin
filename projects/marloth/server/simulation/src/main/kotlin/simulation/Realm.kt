package simulation

import mythic.sculpting.ImmutableFace
import mythic.sculpting.ImmutableMesh
import mythic.spatial.Vector3
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
fun getFaceInfo(face: ImmutableFace): FaceInfo = (face.data as FaceInfo?)!!

val isSolidWall = { face: ImmutableFace ->
  val info = getFaceInfo(face)
  info.type == FaceType.wall && info.texture != null
}
val isSpace = { face: ImmutableFace -> getFaceInfo(face).type == FaceType.space }

fun getNullableFaceInfo(face: ImmutableFace): FaceInfo? = face.data as FaceInfo?

data class Realm(
    val boundary: WorldBoundary,
    val nodes: List<Node>,
    val mesh: ImmutableMesh
) {
//  val faceMap: FaceSectorMap = mutableMapOf()

  val locationNodes: List<Node>
    get() = nodes.filter { it.isWalkable }

  val floors: List<ImmutableFace>
    get() = nodes.flatMap { it.floors }.distinct()

  val walls: List<ImmutableFace>
    get() = nodes.flatMap { it.walls }.distinct()
}

fun initializeFaceInfo(type: FaceType, node: Node, face: ImmutableFace) {
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

fun getOtherNode(node: Node, face: ImmutableFace): Node? {
  val info = getFaceInfo(face)
  return if (info.firstNode == node)
    info.secondNode
  else
    info.firstNode
}

fun getFloor(face: ImmutableFace) =
    face.edges.filter { it.first.z == it.second.z }
        .sortedBy { it.first.z }
        .first()