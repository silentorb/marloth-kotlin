package glowing

import org.lwjgl.opengl.GL

class Glow() {
  val operations = Operations()
  val state = globalState

  init {
    GL.createCapabilities()
  }

}