package rendering

import lab.LabLayout
import lab.renderLab
import mythic.drawing.Canvas
import mythic.drawing.createDrawingMeshes
import mythic.drawing.getUnitScaling
import mythic.glowing.Glow
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

fun renderScene(scene: Scene, painters: Painters, effects: Effects) {
  for (element in scene.elements) {
    painters[element.depiction]!!(element, effects)
  }
}

class Renderer(window: Long) {
  val glow = Glow()
  val shaders = createShaders()
  val vertexSchemas = createVertexSchemas()
  val meshes = createMeshes(vertexSchemas)
  val canvasMeshes = createDrawingMeshes(vertexSchemas.drawing)
  val painters = createPainters(meshes)
  val fonts = loadFonts(listOf(
      FontLoadInfo("cour.ttf", 16, 0f)
  ))

  init {
//    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    glow.state.clearColor = Vector4(1f, 1f, 1f, 1f)
  }

  fun render(scene: Scene, windowInfo: WindowInfo, labLayout: LabLayout) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.operations.clearScreen()
    val effects = createEffects(shaders, gatherEffectsData(windowInfo, scene))
    renderScene(scene, painters, effects)
    val unitScaling = getUnitScaling(windowInfo.dimensions)
    val canvas = Canvas(vertexSchemas.drawing, canvasMeshes, shaders.drawing, unitScaling, windowInfo.dimensions)
    canvas.drawText(TextConfiguration(
        "Welcome to Marloth!",
        fonts[0],
        12f,
        Vector2(100f, 300f),
//        Vector4(1f, 0.8f, 0.3f, 1f)
        Vector4(0f, 0f, 0f, 1f)
    ))

    canvas.drawSolidCircle(Vector2(200f, 200f), 50f, canvas.solid(Vector4(1f, 1f, 0f, 1f)))
    canvas.drawCircle(Vector2(200f, 200f), 50f, canvas.outline(Vector4(1f, 0f, 0f, 1f), 5f))
    canvas.drawLine(100f, 300f, 200f, 320f, Vector4(0f, 0.6f, 0f, 1f), 5f)
    renderLab(labLayout, canvas)
  }

}