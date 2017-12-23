package front

import clienting.Client
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

fun createWindow(): Long {
  glfwDefaultWindowHints() // optional, the current window hints are already the default
  glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
  glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
//  val pid = ProcessHandle.current().getPid()

  val window = glfwCreateWindow(300, 200, "Marloth", NULL, NULL)
  if (window == NULL)
    throw RuntimeException("Failed to create the GLFW window")

  glfwSetKeyCallback(window.toLong()) { window, key, scancode, action, mods ->
    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
      glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
  }

  // Get the thread stack and push a new frame
  stackPush().use { stack ->
    val pWidth = stack.mallocInt(1) // int*
    val pHeight = stack.mallocInt(1) // int*

    glfwGetWindowSize(window, pWidth, pHeight)

    // Get the resolution of the primary monitor
    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    glfwSetWindowPos(
        window.toLong(),
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
    )
  } // the stack frame is popped automatically

  glfwMakeContextCurrent(window.toLong())

  // Enable v-sync
  glfwSwapInterval(1)

  glfwShowWindow(window.toLong())
  return window
}

fun runApp() {
  GLFWErrorCallback.createPrint(System.err).set()
  if (!glfwInit())
    throw IllegalStateException("Unable to initialize GLFW")

  val window = createWindow()
  val client = Client()

  while (!glfwWindowShouldClose(window.toLong())) {
    glfwSwapBuffers(window.toLong()) // swap the color buffers
    client.update()
    glfwPollEvents()
  }
}