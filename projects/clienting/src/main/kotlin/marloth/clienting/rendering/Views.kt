package marloth.clienting.rendering

import silentorb.mythic.characters.rigs.*
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.Body
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.ProjectionType
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.characters.isAliveOrNotACharacter
import simulation.main.Deck

// This is assuming the anchor point is around the torso of the character, not the feet.
val firstPersonCameraOffset = Vector3(0f, 0f, defaultCharacterHeight * 0.4f)
val firstPersonDeadCameraOffset = Vector3(0f, 0f, -0.4f)

const val defaultAngle = 45f

fun emptyCamera() =
    Camera(
        ProjectionType.perspective,
        Vector3.zero,
        Quaternion(),
        defaultAngle
    )

fun debugThirdPersonCameraOffset(orientation: Quaternion) =
    if (getDebugBoolean("THIRD_PERSON_CAMERA"))
      orientation.transform(Vector3(-4f, 0f, -1f))
    else
      Vector3.zero

fun firstPersonCamera(body: Body, character: CharacterRig): Camera = Camera(
    ProjectionType.perspective,
    body.position + firstPersonCameraOffset + debugThirdPersonCameraOffset(character.facingOrientation),
    character.facingOrientation,
    defaultAngle
)

fun deadFirstPersonCamera(body: Body, character: CharacterRig): Camera = Camera(
    ProjectionType.perspective,
    body.position + firstPersonDeadCameraOffset,
    character.facingOrientation * Quaternion().rotateX(Pi / -6f),
    defaultAngle
)

fun thirdPersonCamera(body: Body, thirdPersonRig: ThirdPersonRig): Camera {
  val orientation = getCameraOrientation(thirdPersonRig.rotation)
  val location = getCameraLocation(thirdPersonRig.pivotLocation, orientation, thirdPersonRig.distance)
  return Camera(ProjectionType.perspective, location, orientation, defaultAngle)
}

fun createPlayerCamera(deck: Deck, player: Id): Camera {
  val characterRig = deck.characterRigs[player]!!
  val body = deck.bodies[player]!!
  return if (characterRig.viewMode == ViewMode.firstPerson)
    if (isAliveOrNotACharacter(deck.characters, player))
      firstPersonCamera(body, characterRig)
    else
      deadFirstPersonCamera(body, characterRig)
  else
    thirdPersonCamera(body, deck.thirdPersonRigs[player]!!)
}

