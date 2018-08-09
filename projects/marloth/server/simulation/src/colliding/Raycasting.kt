package colliding

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector3
import mythic.spatial.lineSegmentIntersectsLineSegment
import simulation.Node
import simulation.getFloor
import simulation.getOtherNode

fun raycastNodes(firstNode: Node, start: Vector3, end: Vector3): List<Node> {
  val result = mutableListOf(firstNode)
  var node = firstNode
  var lastWall: FlexibleFace? = null
  do {
    val wall = node.walls.firstOrNull {
      if (it == lastWall)
        false
      else {
        val edge = getFloor(it)
        lineSegmentIntersectsLineSegment(start, end, edge.first, edge.second).first
      }
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