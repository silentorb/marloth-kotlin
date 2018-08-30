package colliding

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3
import mythic.spatial.lineSegmentIntersectsLineSegment
import simulation.*

fun raycastNodes(firstNode: Node, start: Vector3, end: Vector3): List<Node> {
  val result = mutableListOf(firstNode)
  var node = firstNode
  var lastWall: FlexibleFace? = null
  do {
    val wall = node.walls
        .filter { it != lastWall && getFaceInfo(it).type != FaceType.space }
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

fun rayCanHitPoint(firstNode: Node, start: Vector3, end: Vector3): Boolean {
  var node = firstNode
  var lastWall: FlexibleFace? = null
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

    if (getFaceInfo(wall).type != FaceType.space)
      return false

    val nextNode = getOtherNode(node, wall)
    if (nextNode == null)
      return true

    lastWall = wall
    node = nextNode
  } while (true)
}