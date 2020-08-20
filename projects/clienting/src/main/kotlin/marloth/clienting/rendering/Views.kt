package marloth.clienting.rendering

import silentorb.mythic.characters.rigs.*
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.Body
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.ProjectionType
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import simulation.main.Deck

val firstPersonCameraOffset = Vector3(0f, 0f, defaultCharacterHeight * 0.4f)
val firstPersonDeadCameraOffset = Vector3(0f, 0f, -0.75f)

const val defaultAngle = 45f

fun emptyCamera() =
    Camera(
        ProjectionType.perspective,
        Vector3.zero,
        Quaternion(),
        defaultAngle
    )

fun firstPersonCamera(body: Body, character: CharacterRig): Camera = Camera(
    ProjectionType.perspective,
    body.position + firstPersonCameraOffset,
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
  val character = deck.characters[player]!!
  val characterRig = deck.characterRigs[player]!!
  val body = deck.bodies[player]!!
  return if (characterRig.viewMode == ViewMode.firstPerson)
    if (character.isAlive)
      firstPersonCamera(body, characterRig)
    else
      deadFirstPersonCamera(body, characterRig)
  else
    thirdPersonCamera(body, deck.thirdPersonRigs[player]!!)
}
