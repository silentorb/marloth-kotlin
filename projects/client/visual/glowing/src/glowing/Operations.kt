package glowing

import org.lwjgl.opengl.GL11.*

class Operations {

  fun clearScreen() {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer
  }
}