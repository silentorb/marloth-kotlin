package rendering

import mythic.drawing.*
import mythic.glowing.*
import mythic.platforming.DisplayConfig
import mythic.platforming.WindowInfo
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.typography.*
import org.joml.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.GL_TEXTURE_2D_MULTISAMPLE
import org.lwjgl.opengl.GL32.glTexImage2DMultisample
import rendering.meshes.AttributeName
import rendering.meshes.MeshMap
import rendering.meshes.createVertexSchemas
import scenery.GameScene
import scenery.Light
import scenery.Scene
import scenery.Textures
import java.nio.FloatBuffer
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

fun createMultiSampler(glow: Glow, config: DisplayConfig): Multisampler {
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

class Renderer(config: DisplayConfig) {
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
  val meshGenerators = standardMeshes()
  val meshes: MeshMap = createMeshes(vertexSchemas)
  var textures: TextureLibrary = createTextureLibrary(defaultTextureScale)
  val offscreenBuffer = prepareScreenFrameBuffer(config.width, config.height, true)
  val multisampler: Multisampler?
  val fonts = loadFonts(listOf(
      FontLoadInfo(
          filename = "cour.ttf",
          pixelHeight = 16
      )
  ))
  val dynamicMesh = MutableSimpleMesh(vertexSchemas.flat)
  val shaderLookup = Shaders::class.java.kotlin.declaredMemberProperties.map { it.get(shaders) as GeneralPerspectiveShader }

  init {
    glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
    multisampler = if (config.multisamples == 0)
      null
    else
      createMultiSampler(glow, config)
  }

  fun updateShaders(scene: Scene, dimensions: Vector2i, cameraEffectsData: CameraEffectsData) {
    val effectsData = gatherEffectsData(dimensions, scene, cameraEffectsData)
    updateLights(effectsData.lights, sectionBuffer)
    sceneBuffer.load(createSceneBuffer(effectsData))

//    val sceneConfig = SceneShaderConfig(
//        cameraDirection = effectsData.camera.direction,
//        cameraTransform = effectsData.camera.transform
//    )
//
//    for (shader in shaderLookup) {
//      shader.updateScene(sceneConfig)
//    }
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
    val dimensions = Vector2i(offscreenBuffer.colorTexture.width, offscreenBuffer.colorTexture.height)
    glow.state.setFrameBuffer(offscreenBuffer.framebuffer.id)
    glow.state.depthEnabled = true
    glow.state.viewport = Vector4i(0, 0, dimensions.x, dimensions.y)
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
    applyOffscreenBuffer(offscreenBuffer, windowInfo.dimensions, true)
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
