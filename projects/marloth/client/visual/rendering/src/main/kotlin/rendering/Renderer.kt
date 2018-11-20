package rendering

import mythic.breeze.Animation
import mythic.breeze.Bones
import mythic.drawing.*
import mythic.glowing.*
import mythic.platforming.PlatformDisplayConfig
import mythic.platforming.WindowInfo
import mythic.spatial.*
import mythic.typography.*
import org.joml.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE
import org.lwjgl.opengl.GL32.glTexImage2DMultisample
import rendering.meshes.AttributeName
import rendering.meshes.ModelMap
import rendering.meshes.Primitive
import rendering.meshes.createVertexSchemas
import scenery.*
import java.nio.FloatBuffer

enum class TextureAntialiasing {
  none,
  bilinear,
  trilinear
}

data class DisplayConfig(
    override var width: Int = 800,
    override var height: Int = 600,
    override var fullscreen: Boolean = false,
    override var windowedFullscreen: Boolean = false, // Whether fullscreen uses windowed fullscreen
    override var vsync: Boolean = true,
    override var multisamples: Int = 0,
    var depthOfField: Boolean = false,
    var textureAntialiasing: TextureAntialiasing = TextureAntialiasing.trilinear
) : PlatformDisplayConfig

fun gatherEffectsData(dimensions: Vector2i, scene: Scene, cameraEffectsData: CameraEffectsData): EffectsData {
  return EffectsData(
      cameraEffectsData,
      Matrix().ortho(0.0f, dimensions.x.toFloat(), 0.0f, dimensions.y.toFloat(), 0f, 100f),
      scene.lights
  )
}

data class SectorMesh(
    val mesh: SimpleMesh<AttributeName>,
    val textureIndex: List<Textures>
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

const val defaultTextureScale = 1f

data class Multisampler(
    val framebuffer: Framebuffer,
    val renderbuffer: Renderbuffer
)

fun createMultiSampler(glow: Glow, config: PlatformDisplayConfig): Multisampler {
  val texture = Texture(config.width, config.height, null, { width: Int, height: Int, buffer: FloatBuffer? ->
    glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, config.multisamples, GL_RGB, width, height, true)
  }, TextureTarget.multisample)

  val framebuffer: Framebuffer
  val renderbuffer: Renderbuffer

  framebuffer = Framebuffer()
  glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, texture.id, 0)

  renderbuffer = Renderbuffer()
  glRenderbufferStorageMultisample(GL_RENDERBUFFER, config.multisamples, GL_DEPTH24_STENCIL8, config.width, config.height);
  glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, renderbuffer.id);

  checkError("Initializing multisampled framebuffer.")

  val status = glCheckFramebufferStatus(GL_DRAW_FRAMEBUFFER)
  if (status != GL_FRAMEBUFFER_COMPLETE)
    throw Error("Error creating multisample framebuffer.")

  println(status)

  return Multisampler(
      framebuffer = framebuffer,
      renderbuffer = renderbuffer
  )
}

typealias AnimationDurationMap = Map<ArmatureId, Map<AnimationId, Float>>

private val camelCaseRegex = Regex("[-_][a-z]")
fun toCamelCase(identifier: String) =
    identifier.replace(camelCaseRegex) { it.value[1].toUpperCase().toString() }

fun mapAnimationDurations(armatures: Map<ArmatureId, Armature>): AnimationDurationMap =
    armatures
        .mapValues { (_, armature) ->
          armature.animations.mapValues { it.value.duration }
        }

fun mapMeshDepictions(models: ModelMap): Map<MeshId, List<Primitive>> {
  val keys = MeshId.values().associate { Pair(it.name, it) }
  return models.flatMap { entry ->
    val entryName = entry.key.toString()
    val modelKey = keys[entryName]
    if (modelKey != null)
      listOf(Pair(modelKey, entry.value.primitives))
    else
      entry.value.primitives.mapNotNull { primitive ->
        val name = toCamelCase(primitive.name).capitalize()
        val key = keys[name]
        if (key != null)
          Pair(key, listOf(primitive))
        else
          null
      }

  }.associate { it }
}

typealias AnimationMap = Map<AnimationId, Animation>

data class Armature(
    val id: ArmatureId,
    val bones: Bones,
    val animations: AnimationMap,
    val transforms: List<Matrix>
)

//fun mapArmatures(models: ModelMap): Map<ArmatureId, Armature> {
//  val keys = ArmatureId.values().associate { Pair(it.name, it) }
//  return models.mapNotNull { entry ->
//    val key = keys[entry.key.toString()]
//    val armature = entry.value.armature
//    if (key != null && armature != null)
//      Pair(key, armature)
//    else
//      null
//  }.associate { it }
//}

fun textureAttributesFromConfig(config: DisplayConfig) =
    TextureAttributes(
        repeating = true,
        mipmap = config.textureAntialiasing == TextureAntialiasing.trilinear,
        smooth = config.textureAntialiasing != TextureAntialiasing.none
    )

class Renderer(val config: DisplayConfig) {
  val glow = Glow()
  val sceneBuffer = UniformBuffer(sceneBufferSize)
  val sectionBuffer = UniformBuffer(sectionBufferSize)
  val boneBuffer = UniformBuffer(boneBufferSize)
  val shaders = createShaders(UniformBuffers(
      scene = sceneBuffer,
      section = sectionBuffer,
      bone = boneBuffer
  ))
  val drawing = createDrawingEffects()
  val vertexSchemas = createVertexSchemas()
  var worldMesh: WorldMesh? = null
  val meshes: ModelMeshMap
  val armatures: Map<ArmatureId, Armature>
  val animationDurations: AnimationDurationMap
  var mappedTextures: TextureLibrary = createTextureLibrary(defaultTextureScale)
  val textures: DynamicTextureLibrary = createTextureLibrary2(textureAttributesFromConfig(config))
  val offscreenBuffer = prepareScreenFrameBuffer(config.width, config.height, true)
  val multisampler: Multisampler?
  val fonts = loadFonts(listOf(
      FontLoadInfo(
          filename = "fonts/cour.ttf",
          pixelHeight = 16
      )
  ))
  val dynamicMesh = MutableSimpleMesh(vertexSchemas.flat)

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    multisampler = if (config.multisamples == 0)
      null
    else
      createMultiSampler(glow, config)

    val imports = createMeshes(vertexSchemas)
    meshes = imports.first
    armatures = imports.second.associate { Pair(it.id, it) }
    animationDurations = mapAnimationDurations(armatures)
  }

  fun updateShaders(scene: Scene, dimensions: Vector2i, cameraEffectsData: CameraEffectsData) {
    val effectsData = gatherEffectsData(dimensions, scene, cameraEffectsData)
    updateLights(effectsData.lights, sectionBuffer)
    sceneBuffer.load(createSceneBuffer(effectsData))
  }

  fun createSceneRenderer(scene: Scene, viewport: Vector4i): SceneRenderer {
    val dimensions = Vector2i(viewport.z, viewport.w)
    val cameraEffectsData = createCameraEffectsData(dimensions, scene.camera)
    updateShaders(scene, dimensions, cameraEffectsData)
    return SceneRenderer(viewport, this, cameraEffectsData)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    if (multisampler != null) {
      multisampler.framebuffer.activateDraw()
    }
    glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
    glow.state.depthEnabled = true
    glow.operations.clearScreen()
  }

  fun finishRender(windowInfo: WindowInfo) {
    if (multisampler != null) {
      val width = windowInfo.dimensions.x
      val height = windowInfo.dimensions.y
      glow.state.drawFramebuffer = 0
      glow.state.readFramebuffer = multisampler.framebuffer.id
      glDrawBuffer(GL_BACK)                       // Set the back buffer as the draw buffer
      glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL_COLOR_BUFFER_BIT, GL_NEAREST)
    }
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
    val i2 = transform2.transform(Vector4(0f, 0f, 0f, 1f))
    val i = -Vector2(i2.x, i2.y) / i2.z
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

  val meshes: ModelMeshMap
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
