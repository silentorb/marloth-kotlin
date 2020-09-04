package marloth.integration.misc

import marloth.clienting.Client
import marloth.clienting.audio.loadSounds
import marloth.clienting.canvasRendererKey
import marloth.clienting.gatherFontSets
import marloth.clienting.hud.cooldownMeshKey
import marloth.clienting.hud.createCooldownCircleMesh
import marloth.clienting.rendering.createMeshes
import marloth.clienting.rendering.gatherImpModels
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.drawing.setGlobalFonts
import silentorb.mythic.glowing.Glow
import silentorb.mythic.glowing.prepareScreenFrameBuffer
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.VertexSchemas
import silentorb.mythic.lookinglass.meshes.createVertexSchemas
import silentorb.mythic.lookinglass.texturing.*
import silentorb.mythic.platforming.ImageLoader
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.typography.FontSet

fun gatherTextures(loadImage: ImageLoader, attributes: TextureAttributeMapper): List<DeferredTexture> =
    scanTextureResources("models")
        .plus(scanTextureResources("textures"))
        .plus(scanTextureResources("external/images"))
        .map { deferImageFile(loadImage, it, attributes(it)) }

fun gatherTextures(display: PlatformDisplay, displayConfig: DisplayConfig): List<DeferredTexture> {
  val defaultAttributes = textureAttributesFromConfig(displayConfig)
  val backgroundAttributes = defaultAttributes.copy(
      mipmap = false
  )
  val backgroundIds = BackgroundTextureId.values().map { it.name }
  return gatherTextures(display.loadImage) { path ->
    val name = getFileShortName(path)
    if (backgroundIds.contains(name))
      backgroundAttributes
    else
      defaultAttributes
  }
}

fun newRenderer(
    config: DisplayConfig,
    fontSource: () -> List<FontSet>
): Renderer {
  val glow = Glow()
  glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  val vertexSchemas: VertexSchemas = createVertexSchemas()
  val (loadedMeshes, loadedArmatures) = createMeshes(vertexSchemas)
  val meshes = loadedMeshes
  val armatures = loadedArmatures.associateBy { it.id }
  val multisampler = if (config.multisamples == 0)
    null
  else
    createMultiSampler(glow, config.dimensions.x, config.dimensions.y, config.multisamples)

  return Renderer(
      glow = glow,
      config = config,
      fonts = fontSource(),
      meshes = meshes.toMutableMap(),
      armatures = armatures,
      vertexSchemas = vertexSchemas,
      multisampler = multisampler,
      offscreenBuffers = (0..0).map {
        prepareScreenFrameBuffer(config.dimensions.x, config.dimensions.y, true)
      }
  )
}

fun newClient(platform: Platform, displayConfig: DisplayConfig): Client {
  val textures = gatherTextures(platform.display, displayConfig)
  val impModels = if (getDebugBoolean("RENDER_MARCHING"))
    gatherImpModels()
  else
    mapOf()

  val renderer = newRenderer(
      config = displayConfig,
      fontSource = ::gatherFontSets
  )
  setGlobalFonts(renderer.fonts)
  platform.audio.start(50)
  val soundLibrary = loadSounds(platform.audio)
  return Client(
      platform = platform,
      renderer = renderer,
      soundLibrary = soundLibrary,
//      meshLoadingState = MeshLoadingState(impMeshes),
      impModels = impModels,
      textureLoadingState = TextureLoadingState(textures),
      customBloomResources = mapOf(
          cooldownMeshKey to createCooldownCircleMesh(),
          canvasRendererKey to renderer
      )
  )
}
