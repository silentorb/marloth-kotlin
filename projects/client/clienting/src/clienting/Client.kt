package clienting

import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.glfwGetWindowSize
import org.lwjgl.system.MemoryStack
import rendering.Renderer
import rendering.WindowInfo
import scenery.Scene

fun getWindowInfo(window: Long): WindowInfo {
  MemoryStack.stackPush().use { stack ->
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)
    glfwGetWindowSize(window, width, height)

    return WindowInfo(Vector2i(width.get(), height.get()))
  }
}

class Client(val window: Long) {
  private val renderer: Renderer = Renderer()

  fun update(scene: Scene) {
    renderer.render(scene, getWindowInfo(window))
  }

}