package marloth.clienting.rendering.marching

import silentorb.mythic.fathom.mergeDistanceFunctions
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.fathom.transform
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.drawing.drawPrimitive
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector2i

fun renderMarching(renderer: SceneRenderer, models: Map<String, ModelFunction>, camera: Camera, layer: SceneLayer) {
  val marchingElements = layer.elements
      .flatMap { element ->
        element.meshes.filter { mesh ->
          models.containsKey(mesh.mesh)
        }
      }
  if (marchingElements.any()) {
    val distanceFunctions = marchingElements
        .map { mesh ->
          val model = models[mesh.mesh]!!
          transform(mesh.transform, model.form)
        }

    val getDistance = mergeDistanceFunctions(distanceFunctions)
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
//    val coordinates = listOf(Vector2(0.5f, 0.5f))
    val points = coordinates
        .mapNotNull { coordinate ->
          startRay(coordinate, ray)
          val distance = march(config, getDistance, ray, 0f)
          if (distance != null) {
            ray.direction.toVector3() * distance
          } else
            null
        }
    val cube = renderer.meshes["cube"]
    if (cube != null) {
      points.map { offset ->
        drawPrimitive(renderer.renderer, cube.primitives.first(), Matrix.identity.translate(camera.location + offset).scale(0.05f))
      }
    }
  }
}
