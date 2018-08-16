package intellect

import mythic.sculpting.FlexibleFace
import mythic.spatial.Pi
import mythic.spatial.Vector3
import org.joml.plus
import physics.Force
import randomly.Dice
import simulation.*
import simulation.changing.getLookAtAngle

fun getNextPathFace(knowledge: Knowledge, path: Path): FlexibleFace? {
  val character = knowledge.character
  val node = character.body.node
//  val remainingPath = getRemainingPath(node, path)
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

fun updateMovementPursuit(knowledge: Knowledge, pursuit: Pursuit): Pursuit {
  val path =
      if (pursuit.path == null || !pathIsAccessible(knowledge, pursuit.path))
        startRoaming(knowledge)
      else {
        val remainingPath = getRemainingPath(knowledge.character.body.node, pursuit.path)
        if (remainingPath.any())
          remainingPath
        else
          startRoaming(knowledge)
      }

  return Pursuit(path = path)
}

fun moveSpirit(knowledge: Knowledge, pursuit: Pursuit): Actions {
  val character = knowledge.character
  val path = pursuit.path!!
  val face = getNextPathFace(knowledge, path)
  if (face == null) {
    throw Error("Not supported")
//    println("Not supported!!!")
//    return SpiritUpdateResult(spirit.state.copy(
//        path = findPath(node, newPath.last())
//    ))
  } else {
    val edge = getFloor(face)
    val position = character.body.position
    val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
    val target = (edge.middle + nearestPoint) / 2f
//    val target = edge.middle
    val direction = (target - position).normalize()
//    characterMove(spirit.character, direction)
    return listOf(
        Action(ActionType.move, force = Force(offset = direction, body = character.body, maximum = 6f))
    )
  }
}

fun getAngleCourse(source: Float, destination: Float): Float {
  val full = Pi * 2
  if (source == destination)
    return 0f

  val plus = (full + destination - source) % full
  val minus = (full + source - destination) % full
  return if (plus < minus)
    plus
  else
    -minus
}

fun facingDistance(character: Character, lookAt: Vector3): Float {
  val angle = getLookAtAngle(lookAt)
  return getAngleCourse(character.facingRotation.z, angle)
}

//fun movementForce(spirit: Spirit, delta: Float): Force? {
//  val character = spirit.character
//  val course = facingDistance(character, action.offset)
//  val increment = 2f * delta
//  if (Math.abs(course) > increment) {
//    val dir = if (course > 0f) 1f else -1f
//    character.facingRotation.z += increment * dir
//    return null
//  } else {
//    setCharacterFacing(character, action.offset)
//    return Force(spirit.body, action.offset, maximum = 6f)
//  }
//}