package rendering

import mythic.drawing.createDrawingMeshes
import mythic.glowing.*
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.typography.loadFonts
import org.joml.Vector2i
import scenery.Scene
import mythic.spatial.Vector4
import mythic.typography.FontLoadInfo
import org.joml.Vector4i
import org.joml.div
import org.joml.times

fun gatherEffectsData(dimensions: Vector2i, scene: Scene): EffectsData {
  return EffectsData(
      createCameraEffectsData(dimensions, scene.camera),
      Matrix().ortho(0.0f, dimensions.x.toFloat(), 0.0f, dimensions.y.toFloat(), 0f, 100f)
  )
}

data class WorldMesh(
    val mesh: SimpleMesh,
    val textureIndex: List<Texture>
)

fun renderScene(scene: Scene, painters: Painters, effects: Effects, textures: Textures, worldMesh: WorldMesh?) {
  globalState.depthEnabled = true
  if (worldMesh != null) {
    effects.textured.activate(Matrix(), textures.checkers, Vector4(1f), Matrix())
    var index = 0
    for (texture in worldMesh.textureIndex) {
      texture.activate()
      worldMesh.mesh.drawElement(DrawMethod.triangleFan, index++)
    }
  }

  for (element in scene.elements) {
    painters[element.depiction]!!(element, effects)
  }
}

fun getPlayerViewports(playerCount: Int, dimensions: Vector2i): List<Vector4i> {
  val half = dimensions / 2
  return when (playerCount) {
    0, 1 -> listOf(Vector4i(0, 0, dimensions.x, dimensions.y))
    2 -> listOf(
        Vector4i(0, 0, half.x, dimensions.y),
        Vector4i(half.x, 0, half.x, dimensions.y)
    )
    3 -> listOf(
        Vector4i(0, 0, dimensions.x / 2, dimensions.y),
        Vector4i(half.x, half.y, half.x, half.y),
        Vector4i(half.x, 0, half.x, half.y)
    )
    else -> throw Error("Not supported")
  }
}

class Renderer {
  val glow = Glow()
  val shaders = createShaders()
  val vertexSchemas = createVertexSchemas()
  val meshes = createMeshes(vertexSchemas)
  var worldMesh: WorldMesh? = null
  val canvasMeshes = createDrawingMeshes(vertexSchemas.drawing)
  val painters = createPainters(meshes)
  val textures = Textures()
  val fonts = loadFonts(listOf(
      FontLoadInfo("cour.ttf", 16, 0f)
  ))

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
//    glow.state.clearColor = Vector4(1f, 0.95f, 0.9f, 1f)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.state.depthWrite = true
    glow.operations.clearScreen()
  }

  fun renderScene(scene: Scene, effects: Effects) {
    renderScene(scene, painters, effects, textures, worldMesh)
  }

  fun renderedScenes(scenes: List<Scene>, windowInfo: WindowInfo) {
    val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
    for (scene in scenes) {
      val viewport = viewports.next()
      globalState.viewport = viewport
      renderScene(scene, Vector2i(viewport.z, viewport.w))
    }
    globalState.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
  }

  fun createEffects(scene: Scene, dimensions: Vector2i) =
      createEffects(shaders, gatherEffectsData(dimensions, scene))

  fun renderScene(scene: Scene, dimensions: Vector2i) {
    val effects = createEffects(scene, dimensions)
    renderScene(scene, effects)
  }

}