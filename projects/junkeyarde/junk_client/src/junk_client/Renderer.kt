package junk_client

import junk_simulation.World
import mythic.drawing.*
import mythic.platforming.WindowInfo
import mythic.typography.FontLoadInfo
import mythic.typography.loadFonts
import org.joml.Vector2i
import mythic.glowing.*
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextStyle
import org.joml.Vector2f
import org.joml.Vector4i
import org.joml.plus

fun createCanvas(windowInfo: WindowInfo): Canvas {
  val unitScaling = getUnitScaling(windowInfo.dimensions)
  val vertexSchemas = createDrawingVertexSchemas()
  val fonts = loadFonts(listOf(FontLoadInfo("dos.ttf", 8, 0f)))
  return Canvas(
      vertexSchemas,
      createDrawingMeshes(vertexSchemas),
      createDrawingEffects(),
      unitScaling,
      fonts,
      windowInfo.dimensions
  )

}

class Renderer {
  val glow = Glow()

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.state.depthWrite = true
    glow.operations.clearScreen()
  }

  fun finishRender(windowInfo: WindowInfo) {
    globalState.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
  }
}

fun renderScene(renderer: Renderer, world: World, canvas: Canvas, windowInfo: WindowInfo) {
  renderer.prepareRender(windowInfo)
  val textStyle = TextStyle(canvas.fonts[0], 1f, white)
  canvas.drawText("Hello World", Vector2(10f, 10f), textStyle)
  renderer.finishRender(windowInfo)
}
