package generation.abstract

import mythic.sculpting.ImmutableMesh
import simulation.*

/*
class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val isSolid: Boolean,
    val isWalkable: Boolean = false,
    val height: Float = 4f
) : Entity {
  //  val connections: MutableList<Connection> = mutableListOf()
  fun connections(graph: Graph): List<Connection> =
      graph.connections.filter { it.contains(id) }

  val floors: MutableList<ImmutableFace> = mutableListOf()
  val ceilings: MutableList<ImmutableFace> = mutableListOf()
  val walls: MutableList<ImmutableFace> = mutableListOf()

  fun neighbors(graph: Graph): Sequence<Node> = connections(graph).asSequence().mapNotNull { graph.node(it.getOther(this)) }

  fun getConnection(graph: Graph, other: Node) = graph.connections.firstOrNull { it.contains(this) && it.contains(other) }

  fun isConnected(graph: Graph, other: Node) = getConnection(graph, other) != null

  val faces: List<ImmutableFace>
    get() = floors.plus(walls).plus(ceilings)
}
*/

data class Realm(
    val boundary: WorldBoundary,
    val graph: Graph,
    val mesh: ImmutableMesh,
    val nextId: IdSource
) {

  val nodes: List<Node>
    get() = graph.nodes
}

//data class NodeFace(
//    var type: FaceType,
//    val firstNode: Node?,
//    var secondNode: Node?,
//    var texture: Textures? = null,
//    var debugInfo: String? = null
//)

//fun getFaceInfo(face: ImmutableFace): NodeFace = (face.data as NodeFace?)!!

fun faceNodes(info: NodeFace) =
    listOf(info.firstNode, info.secondNode)

//fun getOtherNode(node: Node, face: ImmutableFace): Node? {
//  val info = getFaceInfo(face)
//  return if (info.firstNode == node)
//    info.secondNode
//  else
//    info.firstNode
//}

//fun getNullableFaceInfo(face: ImmutableFace): NodeFace? = face.data as NodeFace?

//fun initializeFaceInfo(type: FaceType, node: Node, face: ImmutableFace) {
//  val info = getNullableFaceInfo(face)
//  face.data =
//      if (info == null) {
//        NodeFace(type, node, null, null)
//      } else {
//        if (info.firstNode == node || info.secondNode == node)
//          face.data
//        else {
////          assert(info.firstNode != null && info.secondNode != null)
//
//          NodeFace(type, info.firstNode, node)
//        }
//      }
//}

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

