package marloth.scenery.creation

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.times
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.ProjectionType
import simulation.entities.HoverCamera
import simulation.main.Deck
import silentorb.mythic.physics.Body
import silentorb.mythic.characters.CharacterRig
import silentorb.mythic.characters.defaultCharacterHeight

val firstPersonCameraOffset = Vector3(0f, 0f, defaultCharacterHeight * 0.25f)
val firstPersonDeadCameraOffset = Vector3(0f, 0f, -0.75f)

fun firstPersonCamera(body: Body, character: CharacterRig, isAlive: Boolean): Camera = Camera(
    ProjectionType.perspective,
//    body.position + Vector3(0f, 3f, -0.75f), //if (isAlive) firstPersonCameraOffset else firstPersonDeadCameraOffset,
    body.position + if (isAlive) firstPersonCameraOffset else firstPersonDeadCameraOffset,
//    world.player.orientation,
    if (isAlive) character.facingQuaternion else character.facingQuaternion * Quaternion().rotateX(Pi / -6f),
    45f
)

fun thirdPersonCamera(body: Body, hoverCamera: HoverCamera): Camera {
  val orientation = Quaternion()
      .rotateZ(hoverCamera.yaw)
      .rotateY(hoverCamera.pitch)

  val offset = orientation * Vector3(hoverCamera.distance, 0f, 0f)
  val orientationSecond = Quaternion().rotateTo(Vector3(1f, 0f, 0f), -offset)
  val position = offset + body.position + Vector3(0f, 0f, 1f)
  return Camera(ProjectionType.perspective, position, orientationSecond, 45f)
}

fun createTopDownCamera(player: Body): Camera {
  val position = Vector3(0f, -20f, 20f) + player.position
  return Camera(
      ProjectionType.perspective,
      position,
      Quaternion().rotate(0f, 0f, Pi * 0.5f)
          *
          Quaternion().rotate(0f, Pi * 0.25f, 0f)
      ,
      45f
  )
}

fun createCamera(deck: Deck, player: Id): Camera {
  val character = deck.characters[player]!!
  val characterRig = deck.characterRigs[player]!!
  val body = deck.bodies[player]!!
  return firstPersonCamera(body, characterRig, character.isAlive)
//  return when (playerRecord.viewMode) {
//    ViewMode.firstPerson -> firstPersonCamera(body, characterRig, character.isAlive)
//    ViewMode.thirdPerson -> thirdPersonCamera(body, playerRecord.hoverCamera)
////    ViewMode.topDown -> createTopDownCamera(body)
//  }
}
