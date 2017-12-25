package visualizing

import scenery.Camera
import scenery.Scene
import simulation.World
import spatial.Quaternion
import spatial.Vector3

enum class CameraMode {
  firstPerson,
  thirdPerson
}

fun createCamera(world: World, cameraMode: CameraMode): Camera = Camera(
    world.player.position,
    world.player.orientation,
    45f
)

fun createScene(world: World, cameraMode: CameraMode): Scene {
  return Scene(
      createCamera(world, cameraMode)
  )
}