package mythic.desktop

import mythic.platforming.Display
import mythic.platforming.DisplayConfig
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.lang.management.ManagementFactory

fun createWindow(title: String, width: Int, height: Int): Long {
  val pid = ManagementFactory.getRuntimeMXBean().getName()
  println("pid: " + pid)
  glfwDefaultWindowHints() // optional, the current window hints are already the default
  glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
  glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
//  val pid = ProcessHandle.current().getPid()

  val window = glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
  if (window == MemoryUtil.NULL)
    throw RuntimeException("Failed to create the GLFW window")

  return window
}

fun centerWindow(window: Long) {
  MemoryStack.stackPush().use { stack ->
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)

    glfwGetWindowSize(window, width, height)

    val videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    glfwSetWindowPos(
        window,
        (videoMode.width() - width.get()) / 2,
        (videoMode.height() - height.get()) / 2
    )
  }
}

fun initializeFullscreen(window: Long) {
  val monitor = glfwGetPrimaryMonitor()
  val videoMode = glfwGetVideoMode(monitor)
  glfwSetWindowMonitor(window, monitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate())
  glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
}

fun initializeWindow(window: Long, config: DisplayConfig) {
  if (config.fullscreen) {
    initializeFullscreen(window)
  } else {
    glfwSetWindowSize(window, config.width, config.height)
    centerWindow(window)
  }

  glfwMakeContextCurrent(window)

  // Enable v-sync
  glfwSwapInterval(1)

  glfwShowWindow(window)
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

  override fun initialize(config: DisplayConfig) = initializeWindow(window, config)

  override fun getInfo(): WindowInfo = getWindowInfo(window)

  override fun swapBuffers() = glfwSwapBuffers(window)
}