package visualizing

import org.joml.AxisAngle4f
import org.joml.times
import scenery.Camera
import scenery.Scene
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
    world.player.position,
//    world.player.orientation,
    Quaternion(0f, 0f, .1f),
    45f
)

fun createOrthographicCamera(world: World): Camera {
  val position = Vector3(0f, -10f, 10f)
  return Camera(
      position,
      Quaternion().rotate(0f, 0f, Pi * 0.5f)
          *
          Quaternion().rotate(0f, Pi * 0.25f, 0f)

      ,
//      Quaternion(0f, Pi * 0.5f, 0f, 0f) * Quaternion(0f, 0f, Pi * -0.5f, 0f),
//      Quaternion(0f, 0f, Pi * -0.5f, 0f) * Quaternion(0f, Pi * 0.5f, 0f, 0f),
//      Matrix()
//          .setLookAt(position, Vector3(), Vector3(0f, 0f, 1f))
//          .getNormalizedRotation(Quaternion()),
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
      createCamera(world, cameraMode)
  )
}