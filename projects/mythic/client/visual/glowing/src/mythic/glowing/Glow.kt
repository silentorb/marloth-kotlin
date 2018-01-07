package mythic.glowing

import org.joml.Vector4i
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*

class Glow() {
  val operations = Operations()
  val state: State
  val glVersion: String

  init {
    GL.createCapabilities()
    glVersion = glGetString(GL_VERSION)
    state = globalState
    val x = 0
  }

}

fun viewportStack(value: Vector4i, action: () -> Unit) {
  val current = globalState.viewport
  globalState.viewport = value
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