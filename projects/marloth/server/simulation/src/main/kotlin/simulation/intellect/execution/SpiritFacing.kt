package simulation.intellect.execution

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.*
import simulation.entities.Character
import simulation.input.Command
import simulation.input.CommandType
import simulation.input.Commands
import simulation.main.World
import simulation.misc.getLookAtAngle
import simulation.updating.simulationDelta
import simulation.misc.*

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

fun spiritFacingChange(world: World, character: Id, offset: Vector3): Commands {
  val characterRecord = world.deck.characters[character]!!
  val course = facingDistance(characterRecord, offset)
  val absCourse = Math.abs(course)
  return if (absCourse == 0f) {
    listOf()
  } else {
    val dir = if (course > 0f) CommandType.lookLeft else CommandType.lookRight
    val velocity = characterRecord.lookVelocity.x
    val increment = characterRecord.turnSpeed.x * simulationDelta
//    println("" + dir + " " + absCourse + " " + increment)
    val drift = increment * velocity / maxPostiveLookVelocityChange()
    return if (absCourse <= drift)
      listOf() // Don't need to rotate anymore.  The remaining momentum will get us there.
    else
      listOf(Command(dir, character, 1f))
  }
}

fun spiritNeedsFacing(world: World, character: Id, offset: Vector3, acceptableRange: Float, action: () -> Commands): Commands {
  val characterRecord = world.deck.characters.getValue(character)
  val facingCommands = spiritFacingChange(world, character, offset)
  val course = facingDistance(characterRecord, offset)
  val absCourse = Math.abs(course)
  return if (absCourse <= acceptableRange)
    facingCommands.plus(action())
  else
    facingCommands
}
