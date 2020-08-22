package marloth.clienting.rendering.marching.services

import marloth.clienting.rendering.marching.*
import silentorb.mythic.fathom.misc.DistanceFunction
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.drawing.drawPrimitive
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector3

fun gatherNeededCells(camera: Camera, getDistance: DistanceFunction): List<Vector3> {
  val startRay = perspectiveRay(camera)
  val ray = newMutableRay()
  val config = MarchingConfig(
      end = camera.farClip,
      maxSteps = 10
  )
  val resolution = Vector2i(12, 8) * 2
  val scale = 3f
  val coordinates = (0 until resolution.y)
      .flatMap { y ->
        (0 until resolution.x)
            .map { x ->
              Vector2(
                  x.toFloat() * scale / resolution.x.toFloat(),
                  y.toFloat() * scale / resolution.y.toFloat()
              ) - scale / 2f
            }
      }

  return coordinates
      .mapNotNull { coordinate ->
        startRay(coordinate, ray)
        val distance = march(config, getDistance, ray, 0f)
        if (distance != null) {
          ray.direction.toVector3() * distance
        } else
          null
      }
}
