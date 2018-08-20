package intellect

import mythic.spatial.*
import simulation.CommandType
import simulation.Character
import simulation.Command
import simulation.Commands
import simulation.changing.getLookAtAngle
import simulation.changing.maxLookVelocityChange
import simulation.changing.simulationDelta

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

fun spiritFacingChange(knowledge: Knowledge, offset: Vector3): Commands {
  val character = knowledge.character
  val course = facingDistance(character, offset)
  val absCourse = Math.abs(course)
  return if (absCourse == 0f) {
    listOf()
  } else {
    val dir = if (course > 0f) CommandType.lookLeft else CommandType.lookRight
    val velocity = character.lookVelocity.x
    val increment = character.turnSpeed.x * simulationDelta
//    println("" + dir + " " + absCourse + " " + increment)
    val drift = increment * velocity / maxLookVelocityChange
    return if (absCourse <= drift)
      listOf() // Don't need to rotate anymore.  The remaining momentum will get us there.
    else
    listOf(Command(dir, knowledge.character.id, 1f))
  }
}