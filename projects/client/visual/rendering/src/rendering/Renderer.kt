package rendering

import glowing.DrawMethod
import glowing.Glow
import org.joml.Vector2i
import scenery.Scene
import spatial.Vector4

data class WindowInfo(val dimensions: Vector2i)

fun gatherEffectsData(windowInfo: WindowInfo, scene: Scene): EffectsData {
  return EffectsData(
      createCameraMatrix(windowInfo.dimensions, scene.camera)
  )
}

fun renderScene(scene: Scene, meshes: MeshMap, effects:Effects) {
  effects.standardEffect.activate()
  meshes["cube"]!!.draw(DrawMethod.triangleFan)
}

class Renderer {
  val glow = Glow()
  val shaders = createShaders()
  val meshes = createMeshes()

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  }

  fun render(scene: Scene, windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.operations.clearScreen()
    val effects = createEffects(shaders, gatherEffectsData(windowInfo, scene))
    renderScene(scene, meshes, effects)
  }

}