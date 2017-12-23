package rendering

import glowing.Glow
import spatial.Vector4

class Renderer {
  val glow = Glow()
  val shaderManager = ShaderManager()
val meshManager = MeshManager()
  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  }

  fun render() {
    glow.operations.clearScreen()
  }

}