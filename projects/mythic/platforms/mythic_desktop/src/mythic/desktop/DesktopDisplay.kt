package mythic.desktop

import mythic.platforming.Display
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.lang.management.ManagementFactory

fun createWindow(title: String, width: Int, height: Int): Long {
//  val arch = is64Bit()
  val pid = ManagementFactory.getRuntimeMXBean().getName()
  println("pid: " + pid)
  glfwDefaultWindowHints() // optional, the current window hints are already the default
  glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
  glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
//  val pid = ProcessHandle.current().getPid()

  val window = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
  if (window == MemoryUtil.NULL)
    throw RuntimeException("Failed to create the GLFW window")

//  glfwSetKeyCallback(window) { window2, key, scancode, action, mods ->
//    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
//      glfwSetWindowShouldClose(window2, true) // We will detect this in the rendering loop
//  }

  MemoryStack.stackPush().use { stack ->
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)

    glfwGetWindowSize(window, width, height)

    // Get the resolution of the primary monitor
    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    glfwSetWindowPos(
        window,
        (vidmode.width() - width.get()) / 2,
        (vidmode.height() - height.get()) / 2
    )
  }

  glfwMakeContextCurrent(window)

  // Enable v-sync
  glfwSwapInterval(1)

  glfwShowWindow(window)
  return window
}

fun getWindowInfo(window: Long): WindowInfo {
  MemoryStack.stackPush().use { stack ->
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)
    glfwGetWindowSize(window, width, height)

    return WindowInfo(Vector2i(width.get(), height.get()))
  }
}

class DesktopDisplay(val window: Long) : Display {

  override fun getInfo(): WindowInfo = getWindowInfo(window)

  override fun swapBuffers() {
    glfwSwapBuffers(window)
  }

}
