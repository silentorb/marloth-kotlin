package mythic.desktop

import mythic.platforming.Platform
import mythic.platforming.PlatformProcess
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFWErrorCallback

class DesktopProcess(val window: Long) : PlatformProcess {

  override fun pollEvents() {
    glfwPollEvents()
  }

  override fun isClosing(): Boolean = GLFW.glfwWindowShouldClose(window)

}

fun createDesktopPlatform(title: String, width: Int, height: Int): Platform {
  GLFWErrorCallback.createPrint(System.err).set()
  if (!glfwInit())
    throw Error("Unable to initialize GLFW")

  val window = createWindow(title, width, height)
  return Platform(
      DesktopDisplay(window),
      DesktopInput(window),
      DesktopProcess(window)
  )
}