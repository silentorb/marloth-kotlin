package marloth.clienting.rendering

import silentorb.mythic.characters.*
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

fun emptyCamera() =
    Camera(
        ProjectionType.perspective,
        Vector3.zero,
        Quaternion(),
        45f
    )

fun firstPersonCamera(body: Body, character: CharacterRig, isAlive: Boolean): Camera = Camera(
    ProjectionType.perspective,
//    body.position + Vector3(0f, 3f, -0.75f), //if (isAlive) firstPersonCameraOffset else firstPersonDeadCameraOffset,
    body.position + if (isAlive) firstPersonCameraOffset else firstPersonDeadCameraOffset,
//    world.player.orientation,
    if (isAlive) character.facingOrientation else character.facingOrientation * Quaternion().rotateX(Pi / -6f),
    45f
)

fun thirdPersonCamera(body: Body, thirdPersonRig: ThirdPersonRig): Camera {
  val orientation = getCameraOrientation(thirdPersonRig.rotation)
  val location = getCameraLocation(thirdPersonRig.pivotLocation, orientation, thirdPersonRig.distance)
  return Camera(ProjectionType.perspective, location, orientation, 45f)
}

fun createCamera(deck: Deck, player: Id): Camera {
  val character = deck.characters[player]!!
  val characterRig = deck.characterRigs[player]!!
  val body = deck.bodies[player]!!
  return if (characterRig.viewMode == ViewMode.firstPerson)
    firstPersonCamera(body, characterRig, character.isAlive)
  else
    thirdPersonCamera(body, deck.thirdPersonRigs[player]!!)
}
