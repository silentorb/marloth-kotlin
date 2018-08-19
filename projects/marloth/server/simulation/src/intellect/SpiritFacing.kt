package intellect

import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.copy
import mythic.spatial.minMax
import simulation.Character
import simulation.Id
import simulation.changing.getLookAtAngle
import simulation.changing.simulationDelta

/*
fun <T>requiresFacing(character: Character, offset: Vector3, delta: Float, delegate: () -> T): T? {
  val course = facingDistance(character, offset)
  val dir = if (course > 0f) 1f else -1f
  val absCourse = Math.abs(course)
  val increment = minMax(2f * delta, -absCourse, absCourse)
  return if (absCourse > increment) {
    character.facingRotation.z += increment * dir
    val facing = character.facingRotation.copy()
    facing.z += increment * dir
    listOf(Action(ActionType.face, facingRotation = facing))
  } else {
    val facing = character.facingRotation.copy()
    facing.z = getLookAtAngle(offset)
    listOf(
        Action(ActionType.face, facingRotation = facing),
        delegate()
    )
  }
}
 */

fun inFacingRange(character: Character, offset: Vector3, delta: Float): Boolean {
  val course = facingDistance(character, offset)
  val absCourse = Math.abs(course)
  val increment = minMax(2f * delta, -absCourse, absCourse)
  return absCourse <= increment
}

fun spiritFacingChange(spirit: Spirit, delta: Float): Vector3? {
  val character = spirit.knowledge.character
  val offset = getTargetOffset(spirit.knowledge, spirit.pursuit)
  val course = facingDistance(character, offset)
  val dir = if (course > 0f) 1f else -1f
  val absCourse = Math.abs(course)
  val increment = minMax(2f * delta, -absCourse, absCourse)
  return if (absCourse > increment) {
    val facing = character.facingRotation.copy()
    facing.z += increment * dir
    facing
  } else {
    val facing = character.facingRotation.copy()
    facing.z = getLookAtAngle(offset)
    facing
  }
}

fun allSpiritFacingChanges(spirits: Collection<Spirit>): Map<Id, Vector3> =
    spirits.mapNotNull { spirit ->
      val result = spiritFacingChange(spirit, simulationDelta)
      if (result != null)
        Pair(spirit.id, result)
      else
        null
    }.associate { it }
