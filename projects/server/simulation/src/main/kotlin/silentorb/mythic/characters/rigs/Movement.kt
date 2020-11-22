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

fun characterMovementToImpulse(actor: Id, requestedOffset: Vector3, characterRig: CharacterRig, speed: Float, velocity: Vector3): LinearImpulse? {
  val airControlMod = if (isGrounded(characterRig)) 1f else airControlReduction
  val commandVector = requestedOffset * speed * airControlMod
  val offset = commandVector * 1.75f + (commandVector - velocity.copy(z = 0f)) * 2f
  return if (offset.length() < 0.005f) {
      null
  } else {
//    val horizontalVelocity = velocity.copy(z = 0f)
//    println("${horizontalVelocity} ${horizontalVelocity.length()}, ${commandVector.length()}, ${offset.length()}, $airControlMod")
    LinearImpulse(body = actor, offset = offset)
  }
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
): List<LinearImpulse> {
  val movementEvents = events
      .filterIsInstance<CharacterRigMovement>()
      .filter {
        hasFreedom(freedomTable[it.actor]
            ?: Freedom.none, Freedom.walking)
      }

  return characterRigs
      .mapNotNull { (actor, rig) ->
        val event = movementEvents.firstOrNull { it.actor == actor }
        val offset = event?.offset ?: Vector3.zero
        characterMovementToImpulse(actor, offset, characterRigs[actor]!!, moveSpeedTable[actor]!!, bodies[actor]!!.velocity)
      }
}
//    events
//        .filterIsInstance<CharacterRigMovement>()
//        .filter {
//          hasFreedom(freedomTable[it.actor]
//              ?: Freedom.none, Freedom.walking)
//        }
//        .map { characterMovementToImpulse(it, characterRigs[it.actor]!!, moveSpeedTable[it.actor]!!, bodies[it.actor]!!.velocity) }
