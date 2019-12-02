package marloth.scenery.creation

import mythic.ent.Id
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.times
import scenery.Camera
import scenery.ProjectionType
import simulation.entities.Character
import simulation.entities.HoverCamera
import simulation.entities.ViewMode
import simulation.main.Deck
import simulation.physics.Body
import simulation.physics.defaultCharacterHeight

val firstPersonCameraOffset = Vector3(0f, 0f, defaultCharacterHeight * 0.25f)
val firstPersonDeadCameraOffset = Vector3(0f, 0f, -0.75f)

fun firstPersonCamera(body: Body, character: Character, isAlive: Boolean): Camera = Camera(
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

fun createCamera(deck: Deck, player: Id): Camera? {
  val character = deck.characters[player]
  if (character == null)
    return null

  val body = deck.bodies[player]!!
  val playerRecord = deck.players[player]!!
  return when (playerRecord.viewMode) {
    ViewMode.firstPerson -> firstPersonCamera(body, character, character.isAlive)
    ViewMode.thirdPerson -> thirdPersonCamera(body, playerRecord.hoverCamera)
//    ViewMode.topDown -> createTopDownCamera(body)
  }
}
