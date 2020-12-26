package marloth.integration.misc

import marloth.clienting.Client
import marloth.clienting.audio.loadSounds
import marloth.clienting.canvasRendererKey
import marloth.clienting.gatherFontSets
import marloth.clienting.gui.hud.cooldownMeshKey
import marloth.clienting.gui.hud.createCooldownCircleMesh
import marloth.clienting.rendering.createMeshes
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
        .plus(scanTextureResources("images"))
        .map { deferImageFile(loadImage, it, attributes(it)) }

fun gatherTextures(display: PlatformDisplay, displayOptions: DisplayOptions): List<DeferredTexture> {
  val defaultAttributes = textureAttributesFromConfig(displayOptions)
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
    options: DisplayOptions,
    fontSource: () -> List<FontSet>
): Renderer {
  val glow = Glow()
  glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  val vertexSchemas: VertexSchemas = createVertexSchemas()
  val (loadedMeshes, loadedArmatures) = createMeshes(vertexSchemas)
  val meshes = loadedMeshes
  val armatures = loadedArmatures.associateBy { it.id }
  val multisampler = if (options.multisamples == 0)
    null
  else
    createMultiSampler(glow, options.windowedResolution.x, options.windowedResolution.y, options.multisamples)

  return Renderer(
      glow = glow,
      options = options,
      fonts = fontSource(),
      meshes = meshes.toMutableMap(),
      armatures = armatures,
      vertexSchemas = vertexSchemas,
      multisampler = multisampler,
      offscreenBuffers = (0..0).map {
        prepareScreenFrameBuffer(options.windowedResolution.x, options.windowedResolution.y, true)
      }
  )
}

fun newClient(platform: Platform, displayOptions: DisplayOptions): Client {
  val textures = gatherTextures(platform.display, displayOptions)
  val renderer = newRenderer(
      options = displayOptions,
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
      textureLoadingState = TextureLoadingState(textures),
      customBloomResources = mapOf(
          cooldownMeshKey to createCooldownCircleMesh(),
          canvasRendererKey to renderer
      )
  )
}
