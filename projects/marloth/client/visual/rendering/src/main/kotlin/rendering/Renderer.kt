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
import scenery.GameScene
import scenery.Light
import scenery.Scene
import kotlin.reflect.full.declaredMemberProperties

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

data class CameraEffectsData(
    val transform: Matrix,
    val direction: Vector3
)

data class EffectsData(
    val camera: CameraEffectsData,
    val flatProjection: Matrix,
    val lights: List<Light>
)

fun mapGameSceneRenderers(renderer: Renderer, scenes: List<GameScene>, windowInfo: WindowInfo): List<GameSceneRenderer> {
  val viewports = getPlayerViewports(scenes.size, windowInfo.dimensions).iterator()
  return scenes.map {
    val viewport = viewports.next()
    GameSceneRenderer(it, renderer.createSceneRenderer(it.main, viewport))
  }
}

class Renderer {
  val glow = Glow()
  val sceneBuffer = UniformBuffer(sceneBufferSize)
  val boneBuffer = UniformBuffer(boneBufferSize)
  val shaders = createShaders(UniformBuffers(sceneBuffer, boneBuffer))
  val drawing = createDrawingEffects()
  val vertexSchemas = createVertexSchemas()
  var worldMesh: WorldMesh? = null
  val meshGenerators = standardMeshes()
  val meshes: MeshMap = createMeshes(vertexSchemas)
  val textures: TextureLibrary = createTextureLibrary(1f)
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

  fun updateShaders(scene: Scene, dimensions: Vector2i, cameraEffectsData: CameraEffectsData) {
//    updateShaders(shaders, gatherEffectsData(dimensions, scene, cameraEffectsData), sectorBuffer)
    val effectsData = gatherEffectsData(dimensions, scene, cameraEffectsData)
    updateLights(effectsData.lights, sceneBuffer)
    val sceneConfig = SceneShaderConfig(
        cameraDirection = effectsData.camera.direction,
        cameraTransform = effectsData.camera.transform,
        sceneBuffer = sceneBuffer
    )

    for (property in Shaders::class.java.kotlin.declaredMemberProperties) {
      val shader = property.get(shaders) as GeneralShader
      shader.updateScene(sceneConfig)
    }
  }

  fun createSceneRenderer(scene: Scene, viewport: Vector4i): SceneRenderer {
    val dimensions = Vector2i(viewport.z, viewport.w)
    val cameraEffectsData = createCameraEffectsData(dimensions, scene.camera)
    updateShaders(scene, dimensions, cameraEffectsData)
    return SceneRenderer(viewport, this, cameraEffectsData)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
    glow.state.depthEnabled = true
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
    val cameraEffectsData: CameraEffectsData
) {

  val effects: Shaders
    get() = renderer.shaders

  fun drawLine(start: Vector3, end: Vector3, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(listOf(start.x, start.y, start.z, end.x, end.y, end.z))

    effects.flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawPoint(position: Vector3, color: Vector4, size: Float = 1f) {
    globalState.pointSize = size
    renderer.dynamicMesh.load(listOf(position.x, position.y, position.z))
    effects.flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.points)
  }

  fun drawSolidFace(vertices: List<Vector3>, color: Vector4) {
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    effects.flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.triangleFan)
  }

  fun drawOutlinedFace(vertices: List<Vector3>, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    effects.flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
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
    val pos = Vector2(((i.x + 1) / 2) * dimensions.x, (1 - ((i.y + 1) / 2)) * dimensions.y)
    val config = TextConfiguration(content, pos, style)
    val textDimensions = calculateTextDimensions(config)
    pos.x -= textDimensions.x / 2f
    val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)
    val transform = prepareTextMatrix(pixelsToScalar, pos)

    drawTextRaw(
        config,
        renderer.drawing.coloredImage,
        renderer.vertexSchemas.drawing.image,
        transform
    )
  }

  val meshes: MeshMap
    get() = renderer.meshes
}

fun createCanvas(renderer: Renderer, windowInfo: WindowInfo): Canvas {
  val unitScaling = getUnitScaling(windowInfo.dimensions)
  return Canvas(
      renderer.drawing,
      unitScaling,
      renderer.fonts,
      windowInfo.dimensions
  )

}
