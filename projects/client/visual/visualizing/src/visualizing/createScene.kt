package visualizing

import org.joml.AxisAngle4f
import org.joml.times
import scenery.Camera
import scenery.Depiction
import scenery.Scene
import scenery.VisualElement
import simulation.World
import spatial.Matrix
import spatial.Pi
import spatial.Quaternion
import spatial.Vector3

enum class CameraMode {
  FirstPerson,
  Orthographic,
  ThirdPerson
}

fun createFirstPersonCamera(world: World): Camera = Camera(
    world.players[0].position,
//    world.player.orientation,
    Quaternion(0f, 0f, .1f),
    45f
)

fun createOrthographicCamera(world: World): Camera {
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

fun createCamera(world: World, cameraMode: CameraMode): Camera = when (cameraMode) {
  CameraMode.FirstPerson -> createFirstPersonCamera(world)
  CameraMode.ThirdPerson -> throw Error("Not implemented")
  CameraMode.Orthographic -> createOrthographicCamera(world)
}

fun createScene(world: World, cameraMode: CameraMode): Scene {
  return Scene(
      createCamera(world, cameraMode),
      world.players.map({ VisualElement(Depiction.child, Matrix().translate(it.position)) })
  )
}