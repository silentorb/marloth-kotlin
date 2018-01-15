package rendering

import lab.LabConfig
import lab.LabLayout
import lab.renderLab
import mythic.drawing.Canvas
import mythic.drawing.createDrawingMeshes
import mythic.drawing.getUnitScaling
import mythic.glowing.DrawMethod
import mythic.glowing.Glow
import mythic.glowing.SimpleMesh
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.typography.loadFonts
import org.joml.Vector2i
import scenery.Scene
import mythic.spatial.Vector4
import mythic.typography.FontLoadInfo
import mythic.typography.TextConfiguration

data class WindowInfo(val dimensions: Vector2i)

fun gatherEffectsData(windowInfo: WindowInfo, scene: Scene): EffectsData {
  return EffectsData(
      createCameraMatrix(windowInfo.dimensions, scene.camera),
      Matrix().ortho(0.0f, windowInfo.dimensions.x.toFloat(), 0.0f, windowInfo.dimensions.y.toFloat(), 0f, 100f)
  )
}

fun renderScene(scene: Scene, painters: Painters, effects: Effects, worldMesh: SimpleMesh?) {
  if (worldMesh != null) {
    effects.standard.activate(Matrix())
    worldMesh.draw(DrawMethod.triangleFan)
  }

  for (element in scene.elements) {
    painters[element.depiction]!!(element, effects)
  }
}

class Renderer(window: Long) {
  val glow = Glow()
  val shaders = createShaders()
  val vertexSchemas = createVertexSchemas()
  val meshes = createMeshes(vertexSchemas)
  var worldMesh: SimpleMesh? = null
  val canvasMeshes = createDrawingMeshes(vertexSchemas.drawing)
  val painters = createPainters(meshes)
  val fonts = loadFonts(listOf(
      FontLoadInfo("cour.ttf", 16, 0f)
  ))

  init {
//    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    glow.state.clearColor = Vector4(1f, 1f, 1f, 1f)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.operations.clearScreen()
  }

  fun renderScene(scene: Scene, windowInfo: WindowInfo) {
    val effects = createEffects(shaders, gatherEffectsData(windowInfo, scene))
    renderScene(scene, painters, effects, worldMesh)
  }

  fun renderLab(windowInfo: WindowInfo, labLayout: LabLayout) {
    val unitScaling = getUnitScaling(windowInfo.dimensions)
    val canvas = Canvas(vertexSchemas.drawing, canvasMeshes, shaders.drawing, unitScaling, windowInfo.dimensions)
    canvas.drawText(TextConfiguration(
        "Dev Lab",
        fonts[0],
        12f,
        Vector2(10f, 10f),
//        Vector4(1f, 0.8f, 0.3f, 1f)
        Vector4(0f, 0f, 0f, 1f)
    ))
    renderLab(labLayout, canvas)
  }

}