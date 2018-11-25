package junk_client

import mythic.bloom.Box
import mythic.bloom.renderLayout
import mythic.drawing.*
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import mythic.glowing.*
import mythic.spatial.Vector4
import mythic.typography.*
import org.joml.Vector4i

fun createCanvas(windowInfo: WindowInfo): Canvas {
  val unitScaling = getUnitScaling(windowInfo.dimensions)
  val vertexSchemas = createDrawingVertexSchemas()
  val dosFont = FontLoadInfo(
      "dos.ttf",
      pixelWidth = 0,
      pixelHeight = 8,
      monospace = 6,
//      loadFlags = FT_LOAD_RENDER or FT_LOAD_TARGET_MONO,
      renderMode = RenderMode.FT_RENDER_MODE_MONO
  )

  val fonts = loadFonts(listOf(dosFont))

  return Canvas(
      createDrawingEffects(),
      unitScaling,
      fonts,
      windowInfo.dimensions
  )
}

//val screenTextureInitializer: SimpleTextureInitializer = { width: Int, height: Int ->
//  GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);
//  GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//  GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//}

class Renderer {
  val glow = Glow()
  val offscreenBuffer: OffscreenBuffer

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    offscreenBuffer = prepareScreenFrameBuffer(320, 200, false)
  }

  fun prepareRender(windowInfo: WindowInfo) {
//    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.state.setFrameBuffer(0)
//    glow.operations.clearScreen()
    glow.state.setFrameBuffer(offscreenBuffer.framebuffer.id)
    glow.state.viewport = Vector4i(0, 0, 320, 200)
    glow.state.depthWrite = false
    glow.operations.clearScreen()
  }

  fun finishRender(windowInfo: WindowInfo, canvas: Canvas) {
//    glow.state.framebuffer = 0
    glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
//    canvas.drawImage(Vector2(), windowInfo.dimensions.toVector2(), canvas.image(offscreenBuffer.texture))
    offscreenBuffer.framebuffer.blitToScreen(Vector2i(320, 200), windowInfo.dimensions, false)
  }
}

fun renderScreen(renderer: Renderer, boxes: List<Box>, canvas: Canvas, windowInfo: WindowInfo, actualWindowInfo: WindowInfo) {
  renderer.prepareRender(windowInfo)
//  val textStyle = TextStyle(canvas.fonts[0], 1f, white)
//  canvas.drawText("Hello World", Vector2(10f, 10f), textStyle)
  renderLayout(boxes, canvas)
  renderer.finishRender(actualWindowInfo, canvas)
}
