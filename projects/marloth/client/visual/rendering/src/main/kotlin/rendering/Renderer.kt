package rendering

import mythic.drawing.*
import mythic.glowing.*
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.*
import org.joml.*
import rendering.meshes.AttributeName
import rendering.meshes.MeshMap
import rendering.meshes.createVertexSchemas
import scenery.*

fun gatherEffectsData(dimensions: Vector2i, scene: Scene, cameraEffectsData: CameraEffectsData): EffectsData {
  return EffectsData(
      cameraEffectsData,
      Matrix().ortho(0.0f, dimensions.x.toFloat(), 0.0f, dimensions.y.toFloat(), 0f, 100f),
      scene.lights
  )
}


data class SectorMesh(
    val mesh: SimpleMesh<AttributeName>,
    val textureIndex: List<Texture>
)

data class WorldMesh(
    val sectors: List<SectorMesh>
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
  val textures = createTextureLibrary()
  val sectorBuffer = UniformBuffer()
  val fonts = loadFonts(listOf(
      FontLoadInfo(
          filename = "cour.ttf",
          pixelHeight = 16
      )
  ))
  val dynamicMesh = MutableSimpleMesh(vertexSchemas.flat)

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
//    glow.state.clearColor = Vector4(1f, 0.95f, 0.9f, 1f)
  }

  fun createEffects(scene: Scene, dimensions: Vector2i, cameraEffectsData: CameraEffectsData) =
      createEffects(shaders, gatherEffectsData(dimensions, scene, cameraEffectsData), sectorBuffer)

  fun createSceneRenderer(scene: Scene, viewport: Vector4i): SceneRenderer {
    val dimensions = Vector2i(viewport.z, viewport.w)
    val cameraEffectsData = createCameraEffectsData(dimensions, scene.camera)
    val effects = createEffects(scene, dimensions, cameraEffectsData)
    return SceneRenderer(viewport, this, effects, cameraEffectsData)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
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
    val effects: Effects,
    val cameraEffectsData: CameraEffectsData
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

  fun drawText(content: String, position: Vector3, style: TextStyle) {
    val modelTransform = Matrix()
        .translate(position + Vector3(0f, 0f, 0f))

    val transform2 = cameraEffectsData.transform * modelTransform
//    val transform2 = modelTransform * cameraEffectsData.transform * modelTransform
    val i2 = transform2.transform(Vector4(0f, 0f, 0f, 1f))
    val i = Vector2(i2.x, i2.y) / i2.z
//    val transform = modelTransform
//    val pixelsToScalar = getUnitScaling(Vector2i(viewport.x, viewport.y))
    val dimensions = Vector2i(viewport.z, viewport.w)
    var pos = Vector2(((i.x + 1) / 2) * dimensions.x, (1 - ((i.y + 1) / 2)) * dimensions.y)
    val config = TextConfiguration(content, pos, style)
    val textDimensions = calculateTextDimensions(config)
    pos.x -= textDimensions.x / 2f
    val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)
    val transform = prepareTextMatrix(pixelsToScalar, pos)

    drawTextRaw(
        config,
        renderer.shaders.drawing.coloredImage,
        renderer.vertexSchemas.drawing.image,
        transform
    )
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
      humanPainter(renderer, mesh.primitives)(element, renderer.effects, childDetails)
    } else {
      val mesh = lookupMesh(element.depiction)
      simplePainter(mesh.primitives)(element, renderer.effects)
    }
  }

  fun renderElements() {
    for (element in scene.elements) {
      renderElement(element)
    }
  }

  fun renderSectorMesh(sector: SectorMesh) {
    var index = 0
    for (texture in sector.textureIndex) {
      texture.activate()
      sector.mesh.drawElement(DrawMethod.triangleFan, index++)
    }
  }

  fun renderWorldMesh() {
    globalState.cullFaces = true
    val worldMesh = renderer.renderer.worldMesh
    if (worldMesh != null) {
      renderer.effects.textured.activate(Matrix(), renderer.renderer.textures[Textures.checkers]!!, Vector4(1f), 0f, Matrix())
      for (sector in worldMesh.sectors) {
        renderSectorMesh(sector)
      }
    }
    globalState.cullFaces = false
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
