package mythic.glowing

import org.joml.Vector2i
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*

class Renderbuffer() {
  val id = glGenRenderbuffers()
  private var disposed = false

  init {
    glBindRenderbuffer(GL_RENDERBUFFER, id)
  }

  fun dispose() {
    if (disposed)
      return

    glDeleteRenderbuffers(id)
    disposed = true
  }
}