package visualizing

import org.joml.times
import scenery.*
import simulation.Player
import simulation.World
import spatial.Matrix
import spatial.Pi
import spatial.Quaternion
import spatial.Vector3

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

fun createOrthographicCamera(): Camera {
  val position = Vector3(0f, -20f, 20f)
  return Camera(
      position,
      Quaternion().rotate(0f, 0f, Pi * 0.5f)
          *
          Quaternion().rotate(0f, Pi * 0.25f, 0f)
      ,
      45f
  )
}

fun createCamera(world: World, screen: Screen): Camera = when (screen.cameraMode) {
  CameraMode.firstPerson -> createFirstPersonCamera(world.players[screen.playerId])
  CameraMode.thirdPerson -> createThirdPersonCamera(world.players[screen.playerId])
  CameraMode.topDown -> createOrthographicCamera()
}

fun createScene(world: World, screen: Screen): Scene {
  return Scene(
      createCamera(world, screen),
      world.players.map({ VisualElement(Depiction.child, Matrix().translate(it.position)) })
  )
}