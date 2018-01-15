package mythic.desktop

import mythic.platforming.Input
import org.lwjgl.glfw.GLFW.*

class DesktopInput(val window: Long) : Input {

  init {
    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1)
  }

 override fun isKeyPressed(key: Int): Boolean = glfwGetKey(window, key) == GLFW_PRESS
}
