package front

import clienting.Client
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import java.nio.*

import org.lwjgl.glfw.Callbacks.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.glfw.GLFW.glfwPollEvents
import java.awt.SystemColor.window
import org.lwjgl.glfw.GLFW.glfwSwapBuffers
import org.lwjgl.opengl.GL11.glClear
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL

fun runApp() {
  // Initialize OpenGL (Display)
  val client = Client()

  GLFWErrorCallback.createPrint(System.err).set()

  // Initialize GLFW. Most GLFW functions will not work before doing this.
  if (!glfwInit())
    throw IllegalStateException("Unable to initialize GLFW")

  // Configure GLFW
  glfwDefaultWindowHints() // optional, the current window hints are already the default
  glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
  glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable
//  val pid = ProcessHandle.current().getPid()
  // Create the window
  val window = glfwCreateWindow(300, 300, "Marloth", NULL, NULL)
  if (window == NULL)
    throw RuntimeException("Failed to create the GLFW window")

  // Setup a key callback. It will be called every time a key is pressed, repeated or released.
  glfwSetKeyCallback(window.toLong()) { window, key, scancode, action, mods ->
    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
      glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
  }

  // Get the thread stack and push a new frame
  stackPush().use { stack ->
    val pWidth = stack.mallocInt(1) // int*
    val pHeight = stack.mallocInt(1) // int*

    // Get the window size passed to glfwCreateWindow
    glfwGetWindowSize(window, pWidth, pHeight)

    // Get the resolution of the primary monitor
    val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

    // Center the window
    glfwSetWindowPos(
        window.toLong(),
        (vidmode.width() - pWidth.get(0)) / 2,
        (vidmode.height() - pHeight.get(0)) / 2
    )
  } // the stack frame is popped automatically

  // Make the OpenGL context current
  glfwMakeContextCurrent(window.toLong())
  // Enable v-sync
  glfwSwapInterval(1)

  // Make the window visible
  glfwShowWindow(window.toLong())

  GL.createCapabilities()

  // Set the clear color
  glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

  // Run the rendering loop until the user has attempted to close
  // the window or has pressed the ESCAPE key.
  while (!glfwWindowShouldClose(window.toLong())) {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer
    glEnable(GL_CULL_FACE)
    glEnable(GL_CULL_FACE)
    glfwSwapBuffers(window.toLong()) // swap the color buffers

    // Poll for window events. The key callback above will only be
    // invoked during this call.
    glfwPollEvents()
  }
}


//    client.free()
