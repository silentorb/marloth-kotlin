package mythic.glowing

import org.joml.Vector4i
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class Glow {
  val operations = Operations()
  val state: State
  val glVersion: String
  val glVendor: String
  val glRenderer: String

  init {
    GL.createCapabilities()
    glVersion = glGetString(GL_VERSION)
    glVendor = glGetString(GL_VENDOR)
    glRenderer = glGetString(GL_RENDERER)
    state = globalState
  }

}

fun viewportStack(value: Vector4i, action: () -> Unit) {
  val current = getGLBounds(GL_VIEWPORT)
  globalState.viewport = value
  checkError("Setting Viewport")
  action()
  globalState.viewport = current
}

fun cropStack(value: Vector4i, action: () -> Unit) {
  val currentBounds = globalState.cropBounds
  val cropEnabled = globalState.cropEnabled
  globalState.cropBounds = value
  globalState.cropEnabled = true
  action()
  globalState.cropBounds = currentBounds
  globalState.cropEnabled = cropEnabled
}