package compuquest_client

import compuquest_client.views.textStyles
import mythic.bloom.next.Box
import mythic.bloom.renderLayout
import mythic.drawing.*
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import mythic.glowing.*
import mythic.spatial.Vector4
import mythic.typography.*
import org.joml.Vector4i

val baseFonts = listOf(
    FontLoadInfo(
        filename = "fonts/dos.ttf",
        pixelHeight = 0
    )
)

fun createCanvas(windowInfo: WindowInfo): Canvas {
  val unitScaling = getUnitScaling(windowInfo.dimensions)
  val vertexSchemas = createDrawingVertexSchemas()
  val fonts = loadFontSets(baseFonts, textStyles)
  setGlobalFonts(fonts)
  return Canvas(
      createDrawingEffects(),
      unitScaling,
      fonts,
      windowInfo.dimensions
  )
}

data class RenderState(
    val glow: Glow = Glow(),
    val offscreenBuffer: OffscreenBuffer,
    val windowLowSize: Vector2i
)

fun newRenderState(windowLowSize: Vector2i): RenderState {
  val glow = Glow()
  glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)

  return RenderState(
      glow = glow,
      offscreenBuffer = prepareScreenFrameBuffer(windowLowSize.x, windowLowSize.y, false),
      windowLowSize = windowLowSize
  )
}


fun prepareRender(renderState: RenderState, windowInfo: WindowInfo) {
  val glow = renderState.glow
  val offscreenBuffer= renderState.offscreenBuffer
//    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
  glow.state.setFrameBuffer(0)
//    glow.operations.clearScreen()
  glow.state.setFrameBuffer(offscreenBuffer.framebuffer.id)
  glow.state.viewport = Vector4i(0, 0, renderState.windowLowSize.x, renderState.windowLowSize.y)
  glow.state.depthWrite = false
  glow.operations.clearScreen()
}

fun finishRender(renderState: RenderState, windowInfo: WindowInfo, canvas: Canvas) {
  val glow = renderState.glow
  val offscreenBuffer= renderState.offscreenBuffer
//    glow.renderState.framebuffer = 0
  glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
//    canvas.drawImage(Vector2(), windowInfo.dimensions.toVector2(), canvas.image(offscreenBuffer.texture))
  offscreenBuffer.framebuffer.blitToScreen(renderState.windowLowSize, windowInfo.dimensions, false)
}


fun renderScreen(renderState: RenderState, box: Box, canvas: Canvas, windowInfo: WindowInfo, actualWindowInfo: WindowInfo) {
  prepareRender(renderState, windowInfo)
//  val textStyle = TextStyle(canvas.fonts[0], 1f, white)
//  canvas.drawText("Hello World", Vector2(10f, 10f), textStyle)
  renderLayout(box, canvas)
  finishRender(renderState, actualWindowInfo, canvas)
}
