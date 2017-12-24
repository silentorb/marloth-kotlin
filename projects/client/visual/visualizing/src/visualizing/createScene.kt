package visualizing

import scenery.Camera
import scenery.Scene
import simulation.World
import spatial.Quaternion
import spatial.Vector3

fun createCamera(world: World): Camera = Camera(
    Vector3(-10f, 0f, 0f),
    Quaternion(),
    45f
)


fun createScene(world: World): Scene {
  return Scene(
      createCamera(world)
  )
}