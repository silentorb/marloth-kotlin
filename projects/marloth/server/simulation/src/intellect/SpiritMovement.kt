package intellect

import simulation.CommandType
import mythic.sculpting.FlexibleFace
import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.copy
import mythic.spatial.minMax
import org.joml.plus
import physics.MovementForce
import randomly.Dice
import simulation.*
import simulation.changing.getLookAtAngle
import simulation.changing.simulationDelta

fun getNextPathFace(knowledge: Knowledge, path: Path): FlexibleFace? {
  val character = knowledge.character
  val node = character.body.node
  val nextNode = path.first()
  return node.walls.firstOrNull { getOtherNode(node, it) == nextNode }
}

fun pathIsAccessible(knowledge: Knowledge, path: Path): Boolean =
    getNextPathFace(knowledge, path) != null

fun startRoaming(knowledge: Knowledge): Path {
  val character = knowledge.character
  val location = character.body.node
  val options = knowledge.nodes
      .filter { it != location && it.isWalkable }

  val destination = Dice.global.getItem(options)
  val path = findPath(location, destination)
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
    val remainingPath = getRemainingPath(knowledge.character.body.node, pursuit.path)
    if (remainingPath.any())
      remainingPath
    else
      startRoaming(knowledge)
  }
}

fun getTargetOffset(knowledge: Knowledge, pursuit: Pursuit): Vector3 {
  val character = knowledge.character
  val path = pursuit.path!!
  val face = getNextPathFace(knowledge, path)
  if (face == null)
    throw Error("Not supported")

  val edge = getFloor(face)
  val position = character.body.position
  val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
  val target = (edge.middle + nearestPoint) / 2f
  return (target - position).normalize()
}

fun moveSpirit(knowledge: Knowledge, pursuit: Pursuit): Commands {
  val offset = getTargetOffset(knowledge, pursuit)
  val character = knowledge.character
  return spiritNeedsFacing(knowledge, offset, 0.1f) {
    listOf(Command(CommandType.moveUp, character.id))
  }
//  val facingCommands = spiritFacingChange(knowledge, offset)
//  val course = facingDistance(character, offset)
//
//  val absCourse = Math.abs(course)
//  val acceptableRange = 0.1f
//  return if (absCourse <= acceptableRange)
//    facingCommands.plus(Command(CommandType.moveUp, character.id))
//  else
//    facingCommands
}