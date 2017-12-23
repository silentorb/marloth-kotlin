package rendering

import glowing.DrawMethod
import glowing.Glow
import spatial.Vector4

class Renderer {
  val glow = Glow()
  val shaders: ShaderMap = createShaders()
  val meshes = createMeshes()

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  }

  fun render() {
    glow.operations.clearScreen()
    shaders["flat"]!!.activate()
    meshes["cube"]!!.draw(DrawMethod.triangleFan)
  }

}