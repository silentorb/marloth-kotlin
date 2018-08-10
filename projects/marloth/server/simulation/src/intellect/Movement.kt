package intellect

import mythic.spatial.Pi
import org.joml.plus
import physics.Force
import randomly.Dice
import simulation.Node
import simulation.World
import simulation.changing.getLookAtAngle
import simulation.changing.setCharacterFacing
import simulation.getFloor
import simulation.getOtherNode

fun setDestination(world: World, spirit: Spirit): SpiritState {
  val location = spirit.body.node
  val options = world.meta.nodes
      .filter { it != location && it.isWalkable }

  val destination = Dice.global.getItem(options)
//  val destination = options[(location.index + 6) % options.size]
  val path = findPath(location, destination)
  assert(path != null)
  assert(path!!.any())
  return spirit.state.copy(
      mode = SpiritMode.moving,
      path = path
  )
}

fun updatePath(node: Node, path: List<Node>): List<Node> {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun moveSpirit(spirit: Spirit): SpiritUpdateResult {
  val node = spirit.body.node
  val newPath = updatePath(node, spirit.state.path!!)

  if (newPath.none())
    return SpiritUpdateResult(SpiritState(SpiritMode.idle))

  val nextNode = newPath.first()
  val face = node.walls.firstOrNull { getOtherNode(node, it) == nextNode }

  if (face == null) {
//    throw Error("Not supported")
//    println("Not supported!!!")
    return SpiritUpdateResult(spirit.state.copy(
        path = findPath(node, newPath.last())
    ))
  } else {
    val edge = getFloor(face)
    val position = spirit.body.position
    val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
    val target = (edge.middle + nearestPoint) / 2f
//    val target = edge.middle
    val direction = (target - position).normalize()
//    characterMove(spirit.character, direction)
    return SpiritUpdateResult(spirit.state, listOf(SpiritAction(SpiritActionType.move, direction)))
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

fun movementForce(spirit: Spirit, action: SpiritAction, delta: Float): Force? {
  val angle = getLookAtAngle(action.offset)
  val character = spirit.character
  val course = getAngleCourse(character.facingRotation.z, angle)
  val increment = 2f * delta
  if (Math.abs(course) > increment) {
    val dir = if (course > 0f) 1f else -1f
    character.facingRotation.z += increment * dir
    return null
  } else {
    setCharacterFacing(character, action.offset)
    return Force(spirit.body, action.offset, maximum = 6f)
  }
}