package marloth.integration.misc

import marloth.clienting.Client
import marloth.clienting.audio.loadSounds
import marloth.clienting.gatherFontSets
import marloth.clienting.hud.cooldownMeshKey
import marloth.clienting.hud.createCooldownCircleMesh
import silentorb.mythic.drawing.setGlobalFonts
import silentorb.mythic.glowing.Glow
import silentorb.mythic.glowing.prepareScreenFrameBuffer
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.VertexSchemas
import silentorb.mythic.lookinglass.meshes.createVertexSchemas
import silentorb.mythic.lookinglass.texturing.AsyncTextureLoader
import silentorb.mythic.lookinglass.texturing.DeferredTexture
import silentorb.mythic.lookinglass.texturing.gatherTextures
import silentorb.mythic.lookinglass.texturing.getFileShortName
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.typography.FontSet

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
  val multisampler = if (config.multisamples == 0)
    null
  else
    createMultiSampler(glow, config)

  val vertexSchemas: VertexSchemas = createVertexSchemas()
  val (loadedMeshes, loadedArmatures) = createMeshes(vertexSchemas)
  val meshes = loadedMeshes
  val armatures = loadedArmatures.associateBy { it.id }
  return Renderer(
      glow = glow,
      config = config,
      fonts = fontSource(),
      meshes = meshes,
      armatures = armatures,
      vertexSchemas = vertexSchemas,
      multisampler = multisampler,
      offscreenBuffers = (0..0).map {
        prepareScreenFrameBuffer(config.width, config.height, true)
      }
  )
}

fun newClient(platform: Platform, displayConfig: DisplayConfig): Client {
  val textures = gatherTextures(platform.display, displayConfig)
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
      textureLoader = AsyncTextureLoader(textures),
      customBloomResources = mapOf(
          cooldownMeshKey to createCooldownCircleMesh()
      )
  )
}
