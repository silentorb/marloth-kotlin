package mythic.glowing

import org.joml.Vector2i
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32

class Framebuffer() {
  val id = glGenFramebuffers()
  private var disposed = false

  init {
    globalState.framebuffer = id
  }

  fun blitToScreen(sourceDimensions: Vector2i, targetDimensions: Vector2i) {
    globalState.readFramebuffer = id
    globalState.drawFramebuffer = 0
    glBlitFramebuffer(
        0, 0, sourceDimensions.x, sourceDimensions.y,
        0, 0, targetDimensions.x, targetDimensions.y,
        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST)
  }

  fun dispose() {
    if (disposed)
      return

    glDeleteFramebuffers(id)
    disposed = true
  }

  fun activateDraw() {
    globalState.drawFramebuffer = id
  }
}

data class OffscreenBuffer(
    val framebuffer: Framebuffer,
    val colorTexture: Texture,
    val depthTexture: Texture?
)

fun prepareScreenFrameBuffer(width:Int, height: Int, withDepth: Boolean): OffscreenBuffer {
  val framebuffer = Framebuffer()
  val textureAttributes = TextureAttributes(
      repeating = false,
      smooth = false,
      storageUnit = TextureStorageUnit.unsigned_byte
  )
  val colorTexture = Texture(width, height, textureAttributes)
  GL32.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, colorTexture.id, 0)
  GL20.glDrawBuffers(GL_COLOR_ATTACHMENT0)

  val depthTexture = if (withDepth) {
    val depthTexture = Texture(width, height, textureAttributes.copy(format = TextureFormat.depth))
    GL32.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture.id, 0)
    depthTexture
  }
  else
    null

  val status = glCheckFramebufferStatus(GL_FRAMEBUFFER)
  if (status != GL_FRAMEBUFFER_COMPLETE)
    throw Error("Error creating framebuffer.")

  globalState.framebuffer = 0
  return OffscreenBuffer(framebuffer, colorTexture, depthTexture)
}
