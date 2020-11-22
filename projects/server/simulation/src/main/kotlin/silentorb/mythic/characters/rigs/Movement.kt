package silentorb.mythic.characters.rigs

import silentorb.mythic.cameraman.characterMovementVector
import silentorb.mythic.cameraman.playerMoveMap
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.*
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.LinearImpulse
import silentorb.mythic.physics.PhysicsDeck
import silentorb.mythic.spatial.*
import simulation.characters.MoveSpeedTable

fun getMovementImpulseVector(baseSpeed: Float, velocity: Vector3, commandVector: Vector3): Vector3 {
  val rawImpulseVector = commandVector * 1.5f - velocity
  val finalImpulseVector = if (rawImpulseVector.length() > baseSpeed)
    rawImpulseVector.normalize() * baseSpeed
  else
    rawImpulseVector

  return finalImpulseVector
}

fun characterMovement(commands: Commands, characterRig: CharacterRig, thirdPersonRig: ThirdPersonRig?, id: Id): CharacterRigMovement? {
  val orientation = if (characterRig.viewMode == ViewMode.firstPerson)
    characterOrientationZ(characterRig)
  else
    hoverCameraOrientationZ(thirdPersonRig!!)

  val offset = characterMovementVector(commands, orientation)
  return if (offset != null)
    CharacterRigMovement(actor = id, offset = offset)
  else
    null
}

fun characterMovementToImpulse(event: CharacterRigMovement, characterRig: CharacterRig, speed: Float, velocity: Vector3): LinearImpulse {
  val airControlMod = if (isGrounded(characterRig)) 1f else airControlReduction
  val commandVector = event.offset * speed * airControlMod
  val horizontalVelocity = velocity.copy(z = 0f)
  val impulseVector = getMovementImpulseVector(speed, horizontalVelocity, commandVector)
  val offset = impulseVector * 5f
  return LinearImpulse(body = event.actor, offset = offset)
}

fun allCharacterMovements(
    deck: PhysicsDeck,
    characterRigs: Table<CharacterRig>,
    thirdPersonRigs: Table<ThirdPersonRig>,
    events: Events
): List<CharacterRigMovement> {
  val commands = events
      .filterIsInstance<Command>()
      .filter { playerMoveMap.keys.contains(it.type) }

  return characterRigs
//      .filter { characterRigs[it.key]!!.isActive }
      .mapNotNull { characterMovement(filterCommands(it.key, commands), it.value, thirdPersonRigs[it.key], it.key) }
}

fun characterMovementsToImpulses(
    bodies: Table<Body>,
    characterRigs: Table<CharacterRig>,
    freedomTable: FreedomTable,
    moveSpeedTable: MoveSpeedTable,
    events: Events
): List<LinearImpulse> =
    events
        .filterIsInstance<CharacterRigMovement>()
        .filter {
          hasFreedom(freedomTable[it.actor]
              ?: Freedom.none, Freedom.walking)
        }
        .map { characterMovementToImpulse(it, characterRigs[it.actor]!!, moveSpeedTable[it.actor]!!, bodies[it.actor]!!.velocity) }
