package intellect

import mythic.ent.Id
import mythic.sculpting.ImmutableFace
import mythic.spatial.Vector3
import org.joml.plus
import randomly.Dice
import simulation.*

fun getNextPathFace(knowledge: Knowledge, path: Path): Id? {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val faces = knowledge.world.realm.faces
  val node = knowledge.world.realm.nodeTable[body.node]!!
  val nextNode = path.first()
  return node.walls.firstOrNull { getOtherNode(node, faces[it]!!) == nextNode.id }
}

fun pathIsAccessible(knowledge: Knowledge, path: Path): Boolean =
    getNextPathFace(knowledge, path) != null

fun startRoaming(knowledge: Knowledge): Path {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val location = knowledge.world.realm.nodeTable[body.node]!!
  val options = knowledge.nodes
      .filter { it != location && it.isWalkable }

  val destination = Dice.global.getItem(options)
  val path = findPath(knowledge.world.realm, location, destination)
  assert(path != null)
  assert(path!!.any())
  assert(pathIsAccessible(knowledge, path))
  return path
}

fun getRemainingPath(node: Node, path: List<Node>): List<Node> {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun updateMovementPursuit(knowledge: Knowledge, pursuit: Pursuit): Path {
  return if (pursuit.path == null || !pathIsAccessible(knowledge, pursuit.path))
    startRoaming(knowledge)
  else {
    val body = knowledge.world.bodyTable[knowledge.spiritId]!!
    val node = knowledge.world.realm.nodeTable[body.node]!!
    val remainingPath = getRemainingPath(node, pursuit.path)
    if (remainingPath.any())
      remainingPath
    else
      startRoaming(knowledge)
  }
}

fun getTargetOffset(knowledge: Knowledge, pursuit: Pursuit): Vector3 {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val path = pursuit.path!!
  val face = getNextPathFace(knowledge, path)
  if (face == null)
    throw Error("Not supported")

  val edge = getFloor(knowledge.world.realm.mesh.faces[face]!!)
  val position = body.position
  val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
  val target = (edge.middle + nearestPoint) / 2f
  return (target - position).normalize()
}

fun moveSpirit(knowledge: Knowledge, pursuit: Pursuit): Commands {
  val offset = getTargetOffset(knowledge, pursuit)
  return spiritNeedsFacing(knowledge, offset, 0.1f) {
    listOf(Command(CommandType.moveUp, knowledge.spiritId))
  }
//  val facingCommands = spiritFacingChange(knowledge, offset)
//  val course = facingDistance(child, offset)
//
//  val absCourse = Math.abs(course)
//  val acceptableRange = 0.1f
//  return if (absCourse <= acceptableRange)
//    facingCommands.plus(Command(CommandType.moveUp, child.id))
//  else
//    facingCommands
}