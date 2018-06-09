package mythic.glowing

import org.joml.Vector2i
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*

class Framebuffer() {
  val id = glGenFramebuffers()
  private var disposed = false

  init {
    glBindFramebuffer(GL_FRAMEBUFFER, id)
  }

  fun blitToScreen(windowDimensions: Vector2i) {
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0)
    glBlitFramebuffer(
        0, 0, 320, 200,
        0, 0, windowDimensions.x, windowDimensions.y,
        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST)
  }

  fun dispose() {
    if (disposed)
      return

    glDeleteFramebuffers(id)
    disposed = true
  }
}