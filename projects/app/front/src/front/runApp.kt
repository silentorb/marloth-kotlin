package front

import clienting.Client
import org.joml.Vector2i
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.system.*;
import java.nio.*

import org.lwjgl.glfw.Callbacks.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.glfw.GLFW.glfwPollEvents
import java.awt.SystemColor.window
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import rendering.WindowInfo
import serving.Server
import visualizing.createScene
import java.lang.management.ManagementFactory

fun is64Bit(): Boolean {
  if (System.getProperty("os.name").contains("Windows")) {
    return System.getenv("ProgramFiles(x86)") != null;
  } else {
    return System.getProperty("os.arch").indexOf("64") != -1;
  }
}

fun createWindow(): Long {
  val arch = is64Bit()
  val pid = ManagementFactory.getRuntimeMXBean().getName()
  glfwDefaultWindowHints() // optional, the current window hints are already the default
  glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
  glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
//  val pid = ProcessHandle.current().getPid()

  val window = glfwCreateWindow(300, 200, "Marloth", NULL, NULL)
  if (window == NULL)
    throw RuntimeException("Failed to create the GLFW window")

  glfwSetKeyCallback(window) { window2, key, scancode, action, mods ->
    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
      glfwSetWindowShouldClose(window2, true) // We will detect this in the rendering loop
  }

  stackPush().use { stack ->
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

fun runApp() {
  GLFWErrorCallback.createPrint(System.err).set()
  if (!glfwInit())
    throw Error("Unable to initialize GLFW")

  val window = createWindow()
  val server = Server()
  val client = Client(window)

  while (!glfwWindowShouldClose(window.toLong())) {
    glfwSwapBuffers(window)
    val scene = createScene(server.world)
    client.update(scene)
    glfwPollEvents()
  }
}