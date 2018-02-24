package mythic.desktop

import mythic.platforming.Platform
import mythic.platforming.PlatformProcess
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.glfwInit
import org.lwjgl.glfw.GLFW.glfwPollEvents
import org.lwjgl.glfw.GLFWErrorCallback

fun is64Bit(): Boolean {
  if (System.getProperty("os.name").contains("Windows")) {
    return System.getenv("ProgramFiles(x86)") != null;
  } else {
    return System.getProperty("os.arch").indexOf("64") != -1;
  }
}

class DesktopProcess(val window: Long) : PlatformProcess {
  override fun close() {
    GLFW.glfwSetWindowShouldClose(window, true)
  }

  override fun pollEvents() {
    glfwPollEvents()
  }

  override fun isClosing(): Boolean = GLFW.glfwWindowShouldClose(window)

}

fun createDesktopPlatform(title: String): Platform {
  GLFWErrorCallback.createPrint(System.err).set()
  if (!glfwInit())
    throw Error("Unable to initialize GLFW")

  val window = createWindow(title, 100, 100)
  return Platform(
      DesktopDisplay(window),
      DesktopInput(window),
      DesktopProcess(window)
  )
}