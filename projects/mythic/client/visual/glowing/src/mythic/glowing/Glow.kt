package mythic.glowing

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class Glow() {
  val operations = Operations()
  val state = globalState
  val glVersion: String

  init {
    GL.createCapabilities()

    glVersion = glGetString(GL_VERSION)
    val x = 0
  }

}