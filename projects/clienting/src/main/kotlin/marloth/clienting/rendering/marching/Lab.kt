package marloth.clienting.rendering.marching

import marloth.clienting.rendering.marching.services.gatherNeededCells
import silentorb.mythic.debugging.setDebugString
import silentorb.mythic.fathom.misc.ModelFunction
import silentorb.mythic.glowing.globalState
import silentorb.mythic.lookinglass.ElementGroups
import silentorb.mythic.lookinglass.SceneLayer
import silentorb.mythic.lookinglass.SceneRenderer
import silentorb.mythic.lookinglass.drawing.drawPrimitive
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Matrix
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector3

fun renderMarchingLab(renderer: SceneRenderer, models: Map<String, ModelFunction>, camera: Camera, allElements: ElementGroups) {
  val elements = filterModels(models, allElements)
  if (elements.any()) {
    val getDistance = elementsToDistanceFunction(models, elements)
    val points = gatherNeededCells(camera, getDistance, listOf(Vector2(0f, 0f)))
    val cube = renderer.meshes["cube"]
    if (cube != null) {
      if (points.any()) {
        setDebugString("${points.first()} ${toCellVector3i(points.first())}")
      }
      globalState.depthEnabled = false
      points.map { offset ->
        drawPrimitive(renderer.renderer, cube.primitives.first(), Matrix.identity.translate(toCellVector3i(offset).toVector3() + 0.5f).scale(0.5f))
        drawPrimitive(renderer.renderer, cube.primitives.first(), Matrix.identity.translate(offset).scale(0.15f), Vector4(1f, 0f, 0f, 1f))
      }
      globalState.depthEnabled = true
    }
  }
}
