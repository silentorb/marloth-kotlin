package rendering

import mythic.drawing.createDrawingMeshes
import mythic.glowing.*
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.typography.loadFonts
import scenery.GameScene
import mythic.spatial.Vector4
import mythic.typography.FontLoadInfo
import org.joml.*
import scenery.Scene

fun gatherEffectsData(dimensions: Vector2i, scene: Scene): EffectsData {
  return EffectsData(
      createCameraEffectsData(dimensions, scene.camera),
      Matrix().ortho(0.0f, dimensions.x.toFloat(), 0.0f, dimensions.y.toFloat(), 0f, 100f),
      scene.lights
  )
}

data class WorldMesh(
    val mesh: SimpleMesh,
    val textureIndex: List<Texture>
)

fun renderScene(scene: GameScene, painters: Painters, effects: Effects, textures: Textures, worldMesh: WorldMesh?) {
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
  var worldMesh: WorldMesh? = null
  val canvasMeshes = createDrawingMeshes(vertexSchemas.drawing)
  val meshes = createMeshes(vertexSchemas)
  val painters = createPainters(meshes)
  val textures = Textures()
  val sectorBuffer = UniformBuffer()
  val fonts = loadFonts(listOf(
      FontLoadInfo("cour.ttf", 16, 0f)
  ))
  val dynamicMesh = MutableSimpleMesh(vertexSchemas.flat)

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
//    glow.state.clearColor = Vector4(1f, 0.95f, 0.9f, 1f)
  }

  fun createEffects(scene: Scene, dimensions: Vector2i) =
      createEffects(shaders, gatherEffectsData(dimensions, scene), sectorBuffer)

  fun createSceneRenderer(scene: Scene, dimensions: Vector2i): SceneRenderer {
    return SceneRenderer(this, worldMesh!!, createEffects(scene, dimensions))
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.state.depthWrite = true
    glow.operations.clearScreen()
  }

  fun renderScenes(scenes: List<GameScene>, windowInfo: WindowInfo) {
    val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
    for (scene in scenes) {
      val viewport = viewports.next()
      globalState.viewport = viewport
      val renderer = createSceneRenderer(scene.main, Vector2i(viewport.z, viewport.w))
      prepareRender(windowInfo)
      renderer.renderScene(scene)
    }
    globalState.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
  }

}

class SceneRenderer(
    val renderer: Renderer,
    val worldMesh: WorldMesh,
    val effects: Effects
) {

  fun renderScene(scene: GameScene) {
    renderScene(scene, renderer.painters, effects, renderer.textures, worldMesh)
  }

  fun drawLine(start: Vector3, end: Vector3, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    val dir = end - start
//    val transform = Matrix()
////        .transformVertices(start)
////        .rotateX(Pi / 2f)
////        .rotateY(-Pi / 2f)
////        .rotateTowards(end - start, Vector3(0f, 0f, 1f))
////        .lookAlong((end - start), Vector3(0f, -1f, 0f))
////        .scale(start.distance(end))
//        .rotateTowards(dir, Vector3(0f, 0f, 1f))
//        .rotateY(-Pi * 0.5f)
    renderer.dynamicMesh.load(listOf(start.x, start.y, start.z, end.x, end.y, end.z))

    effects.flat.activate(Matrix(), color)
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawPoint(position: Vector3, color: Vector4, size: Float = 1f) {
    globalState.pointSize = size
    renderer.dynamicMesh.load(listOf(position.x, position.y, position.z))
    effects.flat.activate(Matrix(), color)
    renderer.dynamicMesh.draw(DrawMethod.points)
  }

  val meshes: MeshMap
    get() = renderer.meshes
}