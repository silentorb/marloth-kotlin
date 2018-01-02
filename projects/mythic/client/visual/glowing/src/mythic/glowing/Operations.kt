package mythic.glowing

import org.joml.Vector2i
import org.lwjgl.opengl.GL11.*

class Operations {

  fun clearScreen() {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer
  }

  fun setViewport(position: Vector2i, dimensions: Vector2i) {
    glViewport(position.x, position.y, dimensions.x, dimensions.y)
  }
}