package intellect.execution

import intellect.acessment.Knowledge
import intellect.acessment.character
import mythic.spatial.*
import simulation.*
import physics.getLookAtAngle
import simulation.simulationDelta

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

fun spiritFacingChange(world: World, knowledge: Knowledge, offset: Vector3): Commands {
  val character = character(world, knowledge)
  val course = facingDistance(character, offset)
  val absCourse = Math.abs(course)
  return if (absCourse == 0f) {
    listOf()
  } else {
    val dir = if (course > 0f) CommandType.lookLeft else CommandType.lookRight
    val velocity = character.lookVelocity.x
    val increment = character.turnSpeed.x * simulationDelta
//    println("" + dir + " " + absCourse + " " + increment)
    val drift = increment * velocity / maxLookVelocityChange()
    return if (absCourse <= drift)
      listOf() // Don't need to rotate anymore.  The remaining momentum will get us there.
    else
      listOf(Command(dir, character(world, knowledge).id, 1f))
  }
}

fun spiritNeedsFacing(world: World, knowledge: Knowledge, offset: Vector3, acceptableRange: Float, action: () -> Commands): Commands {
  val character = character(world, knowledge)
  val facingCommands = spiritFacingChange(world, knowledge, offset)
  val course = facingDistance(character, offset)
  val absCourse = Math.abs(course)
  return if (absCourse <= acceptableRange)
    facingCommands.plus(action())
  else
    facingCommands
}