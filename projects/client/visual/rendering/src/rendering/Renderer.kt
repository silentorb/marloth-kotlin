package rendering

import glowing.DrawMethod
import glowing.Glow
import org.joml.Vector2i
import scenery.Scene
import spatial.Vector4

data class WindowInfo(val dimensions: Vector2i)

class Renderer {
  val glow = Glow()
  val shaders = createShaders()
  val meshes = createMeshes()
  val standardEffect = StandardEffect(shaders.flat)

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