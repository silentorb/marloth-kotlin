package rendering

import mythic.breeze.Bones
import mythic.breeze.SkeletonAnimation
import mythic.drawing.*
import mythic.ent.Id
import mythic.glowing.*
import mythic.platforming.PlatformDisplay
import mythic.platforming.PlatformDisplayConfig
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.*
import org.joml.Vector2i
import org.joml.Vector4i
import org.joml.div
import org.joml.times
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE
import org.lwjgl.opengl.GL32.glTexImage2DMultisample
import rendering.meshes.createVertexSchemas
import rendering.shading.*
import rendering.texturing.*
import scenery.*
import java.nio.ByteBuffer
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
    val id: Id,
    val mesh: SimpleMesh,
    val textureIndex: List<TextureName>
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

fun mapAnimationDurations(armatures: Map<ArmatureId, Armature>): AnimationDurationMap =
    armatures
        .mapValues { (_, armature) ->
          armature.animations.mapValues { it.value.duration }
        }

typealias AnimationMap = Map<AnimationId, SkeletonAnimation>

typealias SocketMap = Map<String, Int>

data class Armature(
    val id: ArmatureId,
    val bones: Bones,
    val animations: AnimationMap,
    val transforms: List<Matrix>,
    val sockets: SocketMap = mapOf()
)

data class ByteTextureBuffer(
    var texture: Texture? = null,
    var buffer: ByteBuffer? = null
)

data class FloatTextureBuffer(
    var texture: Texture? = null,
    var buffer: FloatBuffer? = null
)

fun updateTextureBuffer(dimensions: Vector2i, buffer: ByteTextureBuffer, attributes: () -> TextureAttributes) {
  if (buffer.texture == null) {
    buffer.texture = Texture(dimensions.x, dimensions.y, attributes())
  }
  buffer.texture!!.update(buffer.buffer!!)
}

fun updateTextureBuffer(dimensions: Vector2i, buffer: FloatTextureBuffer, attributes: () -> TextureAttributes) {
  if (buffer.texture == null) {
    buffer.texture = Texture(dimensions.x, dimensions.y, attributes())
  }
  buffer.texture!!.update(buffer.buffer!!)
}

fun textureAttributesFromConfig(config: DisplayConfig) =
    TextureAttributes(
        repeating = true,
        mipmap = config.textureAntialiasing == TextureAntialiasing.trilinear,
        smooth = config.textureAntialiasing != TextureAntialiasing.none,
        storageUnit = TextureStorageUnit.unsigned_byte
    )

class Renderer(
    val config: DisplayConfig,
    display: PlatformDisplay,
    fontList: List<RangedFontLoadInfo>,
    val lightingConfig: LightingConfig
) {
  val glow = Glow()
  var renderColor: ByteTextureBuffer = ByteTextureBuffer()
  var renderDepth: FloatTextureBuffer = FloatTextureBuffer()
  val uniformBuffers = UniformBuffers(
      instance = UniformBuffer(instanceBufferSize),
      scene = UniformBuffer(sceneBufferSize),
      section = UniformBuffer(sectionBufferSize),
      bone = UniformBuffer(boneBufferSize)
  )
  val vertexSchemas = createVertexSchemas()
  val shaders: Shaders = createShaders()
  val shaderCache: ShaderCache = mutableMapOf()
  val getShader = getCachedShader(uniformBuffers, shaderCache)
  val drawing = createDrawingEffects()
  val meshes: ModelMeshMap
  val armatures: Map<ArmatureId, Armature>
  val animationDurations: AnimationDurationMap
  val textures: DynamicTextureLibrary = mutableMapOf()
  val textureLoader = AsyncTextureLoader(gatherTextures(display.loadImage, textureAttributesFromConfig(config)))
  val offscreenBuffers: List<OffscreenBuffer> = (0..0).map {
    prepareScreenFrameBuffer(config.width, config.height, true)
  }
  val multisampler: Multisampler?
  val fonts = loadFontSets(fontList)
  val dynamicMesh = MutableSimpleMesh(vertexSchemas.flat)

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    multisampler = if (config.multisamples == 0)
      null
    else
      createMultiSampler(glow, config)

    val (loadedMeshes, loadedArmatures) = createMeshes(vertexSchemas)
    meshes = loadedMeshes
    armatures = loadedArmatures.associate { Pair(it.id, it) }
    animationDurations = mapAnimationDurations(armatures)
  }

  fun updateShaders(scene: Scene, dimensions: Vector2i, cameraEffectsData: CameraEffectsData) {
    val effectsData = gatherEffectsData(dimensions, scene, cameraEffectsData)
    updateLights(lightingConfig, effectsData.lights, uniformBuffers.section)
    uniformBuffers.scene.load(createSceneBuffer(effectsData))
  }

  fun createSceneRenderer(scene: Scene, viewport: Vector4i): SceneRenderer {
    val dimensions = Vector2i(viewport.z, viewport.w)
    val cameraEffectsData = createCameraEffectsData(dimensions, scene.camera)
    updateShaders(scene, dimensions, cameraEffectsData)
    return SceneRenderer(viewport, this, scene.camera, cameraEffectsData)
  }

  fun prepareRender(windowInfo: WindowInfo) {
    updateAsyncTextureLoading(textureLoader, textures)
    if (multisampler != null) {
      multisampler.framebuffer.activateDraw()
    }
    glow.state.viewport = Vector4i(0, 0, windowInfo.dimensions.x, windowInfo.dimensions.y)
    glow.state.depthEnabled = true
    glow.operations.clearScreen()
    renderColor.buffer = renderColor.buffer
        ?: BufferUtils.createByteBuffer(windowInfo.dimensions.x * windowInfo.dimensions.y * 3)

    renderDepth.buffer = renderDepth.buffer
        ?: BufferUtils.createFloatBuffer(windowInfo.dimensions.x * windowInfo.dimensions.y)
  }

  fun applyRenderBuffer(dimensions: Vector2i) {
    updateTextureBuffer(dimensions, renderColor) {
      TextureAttributes(
          repeating = false,
          smooth = false,
          storageUnit = TextureStorageUnit.unsigned_byte
      )
    }

    updateTextureBuffer(dimensions, renderDepth) {
      TextureAttributes(
          repeating = false,
          smooth = false,
          storageUnit = TextureStorageUnit.float,
          format = TextureFormat.depth
      )
    }

    shaders.screenTexture.activate()
    val canvasDependencies = getStaticCanvasDependencies()
    activateTextures(listOf(renderColor.texture!!, renderDepth.texture!!))
    canvasDependencies.meshes.image.draw(DrawMethod.triangleFan)
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

}

fun rasterizeCoordinates(position: Vector3, cameraEffectsData: CameraEffectsData, dimensions: Vector2i): Vector2 {
  val modelTransform = Matrix()
      .translate(position)

  val transform2 = cameraEffectsData.transform * modelTransform
  val i = transform2.transform(Vector4(0f, 0f, 0f, 1f))
  return Vector2(((i.x + 1) / 2) * dimensions.x, (1 - ((i.y + 1) / 2)) * dimensions.y)
}

class SceneRenderer(
    val viewport: Vector4i,
    val renderer: Renderer,
    val camera: Camera,
    val cameraEffectsData: CameraEffectsData
) {

  val effects: Shaders
    get() = renderer.shaders

  val flat: GeneralPerspectiveShader
  get() = renderer.getShader(renderer.vertexSchemas.flat, ShaderFeatureConfig())

  fun drawLine(start: Vector3, end: Vector3, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(listOf(start.x, start.y, start.z, end.x, end.y, end.z))

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawLines(values: List<Float>, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(values)

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawPoint(position: Vector3, color: Vector4, size: Float = 1f) {
    globalState.pointSize = size
    renderer.dynamicMesh.load(listOf(position.x, position.y, position.z))
    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.points)
  }

  fun drawSolidFace(vertices: List<Vector3>, color: Vector4) {
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.triangleFan)
  }

  fun drawOutlinedFace(vertices: List<Vector3>, color: Vector4, thickness: Float = 1f) {
    globalState.lineThickness = thickness
    renderer.dynamicMesh.load(vertices.flatMap { listOf(it.x, it.y, it.z) })

    flat.activate(ObjectShaderConfig(transform = Matrix(), color = color))
    renderer.dynamicMesh.draw(DrawMethod.lines)
  }

  fun drawText(content: String, position: Vector3, style: TextStyle) {
    val dimensions = Vector2i(viewport.z, viewport.w)
    val pos = rasterizeCoordinates(position, cameraEffectsData, dimensions)
    val config = TextConfiguration(content, pos, style)
    val textDimensions = calculateTextDimensions(config)
    val pos2 = Vector2(pos.x - textDimensions.x / 2f, pos.y)
    val pixelsToScalar = Matrix().scale(1f / dimensions.x, 1f / dimensions.y, 1f)
    val transform = prepareTextMatrix(pixelsToScalar, pos2)

    drawTextRaw(
        config,
        renderer.drawing.coloredImage,
        renderer.vertexSchemas.drawing.image,
        transform
    )
  }

  fun drawCircle(position: Vector3, radius: Float, method: DrawMethod) {
    val resources = getStaticCanvasDependencies()
    val mesh = resources.meshes.circle
    val transform = Matrix()
        .billboardSpherical(position, camera.position, Vector3(0f, 0f, 1f))
        .scale(radius)
    flat.activate(ObjectShaderConfig(
        transform = transform,
        color = Vector4(0.5f, 0.5f, 0f, 0.4f)
    ))

    mesh.draw(method)
  }

  fun drawText(content: String, position: Vector3, style: IndexedTextStyle) =
      drawText(content, position, resolveTextStyle(renderer.fonts, style))

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
