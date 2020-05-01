package simulation.intellect.execution

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.characters.maxPositiveLookVelocityXChange
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.CommandName
import silentorb.mythic.happenings.Commands
import silentorb.mythic.spatial.*
import simulation.main.World
import simulation.updating.simulationDelta
import kotlin.math.abs

fun spiritFacingChange(character: Id, course: Float, velocity: Float, turnSpeed: Float,
                       positiveCommand: CommandName, negativeCommand: CommandName): Commands {
  val absCourse = abs(course)
  return if (absCourse == 0f) {
    listOf()
  } else {
    val dir = if (course > 0f) positiveCommand else negativeCommand
//    val increment = turnSpeed * simulationDelta
//    val overDrift = 2f
//    val drift = overDrift * increment * velocity / maxNegativeLookVelocityXChange()
//    if (absCourse <= drift)
//      listOf() // Don't need to rotate anymore.  The remaining momentum will get us there.
//    else
    listOf(CharacterCommand(dir, character, 1f))
  }
}

fun spiritFacingChange2(character: Id, course: Float, velocity: Float, turnSpeed: Float,
                        positiveCommand: CommandName, negativeCommand: CommandName): Commands {
  val absCourse = abs(course)
  return if (absCourse == 0f) {
    listOf()
  } else {
    val dir = if (course > 0f) positiveCommand else negativeCommand
    val increment = turnSpeed * simulationDelta
    val drift = increment * velocity / maxPositiveLookVelocityXChange()
    if (absCourse <= drift)
      listOf() // Don't need to rotate anymore.  The remaining momentum will get us there.
    else
      listOf(CharacterCommand(dir, character, 1f))
  }
}

fun spiritHorizontalFacingChange(world: World, character: Id, offset: Vector3): Commands {
  val characterRig = world.deck.characterRigs[character]!!
  val course = horizontalFacingDistance(characterRig.facingRotation.x, offset)
//  println("$course ${characterRig.facingRotation.z}")
  return spiritFacingChange(character, course, characterRig.firstPersonLookVelocity.x, characterRig.turnSpeed.x,
      CharacterCommands.lookLeft, CharacterCommands.lookRight)
}

fun spiritVerticalFacingChange(world: World, character: Id, offset: Vector3): Commands {
  val characterRig = world.deck.characterRigs[character]!!
  val course = verticalFacingDistance(characterRig.facingRotation.y, offset)
//  println("$course ${characterRig.facingRotation.y}")
  return spiritFacingChange2(character, course, characterRig.firstPersonLookVelocity.y, characterRig.turnSpeed.y,
      CharacterCommands.lookUp, CharacterCommands.lookDown)
}

fun spiritNeedsFacing(world: World, character: Id, offset: Vector3, acceptableRange: Float, action: () -> Commands): Commands {
  val characterRig = world.deck.characterRigs.getValue(character)
  val facingCommands = spiritHorizontalFacingChange(world, character, offset)
      .plus(spiritVerticalFacingChange(world, character, offset))
  val horizontalCourse = horizontalFacingDistance(characterRig.facingRotation.x, offset)
  val verticalCourse = verticalFacingDistance(characterRig.facingRotation.y, offset)
  return if (abs(horizontalCourse) <= acceptableRange && abs(verticalCourse) <= acceptableRange)
    facingCommands.plus(action())
  else
    facingCommands
}
