package mythic.desktop

import mythic.platforming.PlatformDisplay
import mythic.platforming.PlatformDisplayConfig
import mythic.platforming.WindowInfo
import org.joml.Vector2i
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

fun createWindow(title: String, config: PlatformDisplayConfig): Long {
//  val pid = ManagementFactory.getRuntimeMXBean().getName()
//  println("pid: " + pid)
  glfwDefaultWindowHints() // optional, the current window hints are already the default
  glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
  glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
  glfwWindowHint(GLFW_SAMPLES, config.multisamples)
//  val pid = ProcessHandle.current().getPid()

  val window = glfwCreateWindow(config.width, config.height, title, MemoryUtil.NULL, MemoryUtil.NULL)
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
  glfwFocusWindow(window) // For some reason the window loses focus when switching to fullscreen mode?
}

fun initializeWindow(window: Long, config: PlatformDisplayConfig) {
  if (config.fullscreen) {
    initializeFullscreen(window)
  } else {
    glfwSetWindowSize(window, config.width, config.height)
    centerWindow(window)
  }

  glfwMakeContextCurrent(window)

  // Enable v-sync
  glfwSwapInterval(if (config.vsync) 1 else 0)

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

class DesktopDisplay(val window: Long) : PlatformDisplay {

  override fun initialize(config: PlatformDisplayConfig) = initializeWindow(window, config)

  override fun getInfo(): WindowInfo = getWindowInfo(window)

  override fun swapBuffers() = glfwSwapBuffers(window)

  override fun hasFocus() = glfwGetWindowAttrib(window, GLFW_FOCUSED) == 1
}