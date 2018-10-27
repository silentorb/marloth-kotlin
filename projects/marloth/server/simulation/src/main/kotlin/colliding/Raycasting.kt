package colliding

import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3
import mythic.spatial.lineSegmentIntersectsLineSegment
import simulation.*

/*
fun raycastNodes(firstNode: Node, start: Vector3, end: Vector3): List<Node> {
  val result = mutableListOf(firstNode)
  var node = firstNode
  var lastWall: ImmutableFace? = null
  do {
    val wall = node.walls
        .filter { it != lastWall && getFaceInfo(it).faceType != FaceType.space }
        .firstOrNull {
          val edge = getFloor(it)
          lineSegmentIntersectsLineSegment(start, end, edge.first, edge.second).first
        }
    if (wall == null)
      break

    val nextNode = getOtherNode(node, wall)
    if (nextNode == null)
      break

    lastWall = wall
    node = nextNode
    result.add(node)
  } while (true)
  return result
}
*/

fun rayCanHitPoint(nodes: NodeTable, faces: FaceTable, firstNode: Node, start: Vector3, end: Vector3): Boolean {
  var node = firstNode
  var lastWall: ImmutableFace? = null
  do {
    val walls = node.walls
        .filter { it != lastWall }

    val wall = walls
        .firstOrNull {
          val edge = getFloor(it)
          lineSegmentIntersectsLineSegment(start, end, edge.first, edge.second).first
        }
    if (wall == null)
      return true

    if (faces[wall.id]!!.faceType != FaceType.space)
      return false

    val nextNode = nodes[getOtherNode(node,faces[ wall.id]!!)]
    if (nextNode == null)
      return true

    lastWall = wall
    node = nextNode
  } while (true)
}