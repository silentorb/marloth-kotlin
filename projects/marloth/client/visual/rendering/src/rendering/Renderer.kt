package rendering

import mythic.drawing.drawText
import mythic.drawing.getUnitScaling
import mythic.glowing.Glow
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.typography.loadFonts
import org.joml.Vector2i
import scenery.Scene
import mythic.spatial.Vector4
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
  val painters = createPainters(meshes)
  val fonts = loadFonts(listOf("lo-fi.ttf"))

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  }

  fun render(scene: Scene, windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.operations.clearScreen()
    val effects = createEffects(shaders, gatherEffectsData(windowInfo, scene))
    renderScene(scene, painters, effects)
    val unitScaling = getUnitScaling(windowInfo.dimensions)
    drawText(TextConfiguration(
        "Welcome to Marloth!",
        fonts[0],
        40f,
        Vector2(100f, 300f),
        Vector4(1f, 0.8f, 0f, 1f)
    ), effects.coloredImage, vertexSchemas.coloredImage, unitScaling)
//    canvasManager.drawText("Dev Lab", 10f, 10f)
  }

}