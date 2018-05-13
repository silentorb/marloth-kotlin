package rendering

import mythic.drawing.Canvas
import mythic.drawing.createDrawingMeshes
import mythic.drawing.getUnitScaling
import mythic.glowing.*
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.typography.loadFonts
import scenery.GameScene
import mythic.spatial.Vector4
import mythic.typography.FontLoadInfo
import org.joml.*
import rendering.meshes.AttributeName
import rendering.meshes.MeshMap
import rendering.meshes.createVertexSchemas
import scenery.DepictionType
import scenery.Scene
import scenery.VisualElement

fun gatherEffectsData(dimensions: Vector2i, scene: Scene): EffectsData {
  return EffectsData(
      createCameraEffectsData(dimensions, scene.camera),
      Matrix().ortho(0.0f, dimensions.x.toFloat(), 0.0f, dimensions.y.toFloat(), 0f, 100f),
      scene.lights
  )
}

data class WorldMesh(
    val mesh: SimpleMesh<AttributeName>,
    val textureIndex: List<Texture>
)

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

fun mapGameSceneRenderers(renderer: Renderer, scenes: List<GameScene>, windowInfo: WindowInfo): List<GameSceneRenderer> {
  val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
  return scenes.map {
    val viewport = viewports.next()
    GameSceneRenderer(it, renderer.createSceneRenderer(it.main, viewport))
  }
}

class Renderer {
  val glow = Glow()
  val shaders = createShaders()
  val vertexSchemas = createVertexSchemas()
  var worldMesh: WorldMesh? = null
  val canvasMeshes = createDrawingMeshes(vertexSchemas.drawing)
  val meshGenerators = standardMeshes()
  val meshes = createMeshes(vertexSchemas)
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

  fun createSceneRenderer(scene: Scene, viewport: Vector4i): SceneRenderer {
    return SceneRenderer(viewport, this, createEffects(scene, Vector2i(viewport.z, viewport.w)))
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.operations.setViewport(Vector2i(0, 0), windowInfo.dimensions)
    glow.state.depthWrite = true
    glow.operations.clearScreen()
  }

  fun finishRender(windowInfo: WindowInfo) {
    globalState.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
  }

  fun renderGameScenes(scenes: List<GameScene>, windowInfo: WindowInfo) {
    prepareRender(windowInfo)
    val renderers = mapGameSceneRenderers(this, scenes, windowInfo)
    renderers.forEach { it.render() }
    finishRender(windowInfo)
  }

}

class SceneRenderer(
    val viewport: Vector4i,
    val renderer: Renderer,
    val effects: Effects
) {

  fun drawLine(start: Vector3, end: Vector3, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
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

  fun drawSolidFace(vertices: List<Vector3>, color: Vector4) {
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    effects.flat.activate(Matrix(), color)
    renderer.dynamicMesh.draw(DrawMethod.triangleFan)
  }

  fun drawOutlinedFace(vertices: List<Vector3>, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    effects.flat.activate(Matrix(), color)
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  val meshes: MeshMap
    get() = renderer.meshes
}

class GameSceneRenderer(
    val scene: GameScene,
    val renderer: SceneRenderer
) {

  fun prepareRender() {
    globalState.viewport = renderer.viewport
    globalState.depthEnabled = true
  }

  fun lookupMesh(depiction: DepictionType) = renderer.meshes[simplePainterMap[depiction]]!!

  fun renderElement(element: VisualElement) {
    val childDetails = scene.elementDetails.children[element.id]
    if (childDetails != null) {
      val mesh = lookupMesh(element.depiction)
      humanPainter(mesh)(element, renderer.effects, childDetails)
    } else {
      val mesh = lookupMesh(element.depiction)
      simplePainter(mesh)(element, renderer.effects)
    }
  }

  fun renderElements() {
    for (element in scene.elements) {
      renderElement(element)
    }
  }

  fun renderWorldMesh() {
    val worldMesh = renderer.renderer.worldMesh
    if (worldMesh != null) {
      renderer.effects.textured.activate(Matrix(), renderer.renderer.textures.checkers, Vector4(1f), 0f, Matrix())
      var index = 0
      for (texture in worldMesh.textureIndex) {
        texture.activate()
        worldMesh.mesh.drawElement(DrawMethod.triangleFan, index++)
      }
    }
  }

  fun render() {
    prepareRender()
    renderWorldMesh()
    renderElements()
  }

}

fun createCanvas(renderer: Renderer, windowInfo: WindowInfo): Canvas {
  val unitScaling = getUnitScaling(windowInfo.dimensions)
  return Canvas(
      renderer.vertexSchemas.drawing,
      renderer.canvasMeshes,
      renderer.shaders.drawing,
      unitScaling,
      renderer.fonts,
      windowInfo.dimensions
  )

}
