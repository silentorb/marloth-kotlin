package rendering

import glowing.DrawMethod
import glowing.Glow
import glowing.MatrixProperty
import glowing.ShaderProgram
import org.joml.Vector2i
import org.joml.Vector3i
import scenery.Scene
import spatial.Matrix
import spatial.Vector4

data class WindowInfo(val dimensions: Vector2i)

class StandardEffect(val shader: ShaderProgram) {
  val cameraMatrix = MatrixProperty(shader, "cameraMatrix")
  fun activate(camera: Matrix) {
    shader.activate()
    cameraMatrix.value = camera
  }
}

class Renderer {
  val glow = Glow()
  val shaders: ShaderMap = createShaders()
  val meshes = createMeshes()
  val standardEffect = StandardEffect(shaders["flat"]!!)

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  }

  fun render(scene: Scene, windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.operations.clearScreen()
    val cameraMatrix = createCameraMatrix(windowInfo.dimensions, scene.camera)
    standardEffect.activate(cameraMatrix)
    meshes["cube"]!!.draw(DrawMethod.triangleFan)
  }

}