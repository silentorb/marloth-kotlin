package visualizing

import org.joml.times
import scenery.*
import simulation.Player
import simulation.World
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import org.joml.plus

fun createFirstPersonCamera(player: Player): Camera = Camera(
    player.position,
//    world.player.orientation,
    Quaternion(0f, 0f, .1f),
    45f
)

fun createThirdPersonCamera(player: Player): Camera = Camera(
    player.position,
    Quaternion(0f, 0f, .1f),
    45f
)

fun createOrthographicCamera(player: Player): Camera {
  val position = Vector3(0f, -20f, 20f) + player.position
  return Camera(
      position,
      Quaternion().rotate(0f, 0f, Pi * 0.5f)
          *
          Quaternion().rotate(0f, Pi * 0.25f, 0f)
      ,
      45f
  )
}

fun createCamera(world: World, screen: Screen): Camera {
  val mainPlayer = world.players[screen.playerId]
  return when (screen.cameraMode) {
    CameraMode.firstPerson -> createFirstPersonCamera(mainPlayer)
    CameraMode.thirdPerson -> createThirdPersonCamera(mainPlayer)
    CameraMode.topDown -> createOrthographicCamera(mainPlayer)
  }
}

fun createScene(world: World, screen: Screen): Scene {
  return Scene(
      createCamera(world, screen),
      world.players.map({ VisualElement(Depiction.child, Matrix().translate(it.position)) })
  )
}