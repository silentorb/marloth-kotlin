package colliding

import mythic.ent.Id
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

fun rayCanHitPoint(realm: Realm, firstNode: Node, start: Vector3, end: Vector3): Boolean {
  // TODO: Needs updating
  return false
//  var node = firstNode
//  var lastWall: Id? = null
//  do {
//    val walls = node.walls
//        .filter { it != lastWall }
//
//    val wall = walls
//        .firstOrNull {
//          val edge = getFloor(realm.mesh.faces[it]!!)
//          lineSegmentIntersectsLineSegment(start, end, edge.first, edge.second).first
//        }
//    if (wall == null)
//      return true
//
//    val info = realm.faces[wall]!!
//
//    if (info.faceType != FaceType.space)
//      return false
//
//    val nextNode = realm.nodeTable[getOtherNode(node.id, info)]
//    if (nextNode == null)
//      return true
//
//    lastWall = wall
//    node = nextNode
//  } while (true)
}
