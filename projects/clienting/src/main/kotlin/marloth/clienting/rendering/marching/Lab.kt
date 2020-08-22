package marloth.clienting.rendering.marching

import marloth.clienting.rendering.marching.services.gatherNeededCells
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.drawing.drawPrimitive
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Matrix

fun renderMarchingLab(renderer: SceneRenderer, models: Map<String, ModelFunction>, camera: Camera, layer: SceneLayer) {
  val elements = filterModels(models, layer.elements)
  if (elements.any()) {
    val getDistance = elementsToDistanceFunction(models, elements)
    val points = gatherNeededCells(camera, getDistance)
    val cube = renderer.meshes["cube"]
    if (cube != null) {
      points.map { offset ->
        drawPrimitive(renderer.renderer, cube.primitives.first(), Matrix.identity.translate(camera.location + offset).scale(0.05f))
      }
    }
  }
}
