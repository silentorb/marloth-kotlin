package simulation.intellect.execution

import silentorb.mythic.ent.Id
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.spatial.*
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.CommonCharacterCommands
import silentorb.mythic.happenings.Commands
import simulation.main.World
import silentorb.mythic.characters.getLookAtAngle
import silentorb.mythic.characters.maxPositiveLookVelocityChange
import simulation.updating.simulationDelta

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

fun facingDistance(character: CharacterRig, lookAt: Vector3): Float {
  val angle = getLookAtAngle(lookAt)
  return getAngleCourse(character.facingRotation.z, angle)
}

fun spiritFacingChange(world: World, character: Id, offset: Vector3): Commands {
  val characterRig = world.deck.characterRigs[character]!!
  val course = facingDistance(characterRig, offset)
  val absCourse = Math.abs(course)
  return if (absCourse == 0f) {
    listOf()
  } else {
    val dir = if (course > 0f) CommonCharacterCommands.lookLeft else CommonCharacterCommands.lookRight
    val velocity = characterRig.lookVelocity.x
    val increment = characterRig.turnSpeed.x * simulationDelta
//    println("" + dir + " " + absCourse + " " + increment)
    val drift = increment * velocity / maxPositiveLookVelocityChange()
    return if (absCourse <= drift)
      listOf() // Don't need to rotate anymore.  The remaining momentum will get us there.
    else
      listOf(CharacterCommand(dir, character, 1f))
  }
}

fun spiritNeedsFacing(world: World, character: Id, offset: Vector3, acceptableRange: Float, action: () -> Commands): Commands {
  val characterRig = world.deck.characterRigs.getValue(character)
  val facingCommands = spiritFacingChange(world, character, offset)
  val course = facingDistance(characterRig, offset)
  val absCourse = Math.abs(course)
  return if (absCourse <= acceptableRange)
    facingCommands.plus(action())
  else
    facingCommands
}
