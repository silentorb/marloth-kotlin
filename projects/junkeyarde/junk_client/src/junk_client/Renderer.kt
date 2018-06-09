package junk_client

import junk_simulation.World
import mythic.bloom.Box
import mythic.bloom.renderLayout
import mythic.drawing.*
import mythic.platforming.WindowInfo
import mythic.typography.FontLoadInfo
import mythic.typography.loadFonts
import org.joml.Vector2i
import mythic.glowing.*
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.typography.TextStyle
import org.joml.Vector2f
import org.joml.Vector4i
import org.joml.plus
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glDrawBuffers
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.glFramebufferTexture
import java.nio.FloatBuffer

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

val screenTextureInitializer: SimpleTextureInitializer = { width: Int, height: Int ->
  glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, 320, 200, 0, GL_RGB, GL_UNSIGNED_BYTE, 0);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
}

data class OffscreenBuffer(
    val framebuffer: Framebuffer,
    val texture: Texture
)

fun prepareScreenFrameBuffer(): OffscreenBuffer {
  val framebuffer = Framebuffer()
  val texture = Texture(320, 200, screenTextureInitializer)
  glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texture.id, 0);
  glDrawBuffers(GL_COLOR_ATTACHMENT0)
  if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
    throw Error("Error creating framebuffer.")

  globalState.framebuffer = 0
  return OffscreenBuffer(framebuffer, texture)
}

class Renderer {
  val glow = Glow()
  val offscreenBuffer: OffscreenBuffer

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    offscreenBuffer = prepareScreenFrameBuffer()
  }

  fun prepareRender(windowInfo: WindowInfo) {
//    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.state.framebuffer = offscreenBuffer.framebuffer.id
    glow.state.viewport = Vector4i(0, 0, 320, 200)
    glow.state.depthWrite = false
    glow.operations.clearScreen()
  }

  fun finishRender(windowInfo: WindowInfo, canvas: Canvas) {
//    glow.state.framebuffer = 0
    glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
//    canvas.drawImage(Vector2(), windowInfo.dimensions.toVector2(), canvas.image(offscreenBuffer.texture))
    offscreenBuffer.framebuffer.blitToScreen(windowInfo.dimensions)
  }
}

fun renderScreen(renderer: Renderer, boxes: List<Box>, canvas: Canvas, windowInfo: WindowInfo) {
  renderer.prepareRender(windowInfo)
//  val textStyle = TextStyle(canvas.fonts[0], 1f, white)
//  canvas.drawText("Hello World", Vector2(10f, 10f), textStyle)
  renderLayout(boxes, canvas)
  renderer.finishRender(windowInfo, canvas)
}
