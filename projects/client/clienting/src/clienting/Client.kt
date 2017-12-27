package clienting

import commanding.Commands
import haft.InputRoot
import haft.createNewInputRoot
import haft.getCommands
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
  private val config: Configuration = createNewConfiguration()
  private val inputRoot: InputRoot = createNewInputRoot(window, config.input)

  fun update(scene: Scene): Commands {
    renderer.render(scene, getWindowInfo(window))
    return getCommands(inputRoot)
  }

}