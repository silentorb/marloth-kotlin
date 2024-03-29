package marloth.clienting

import marloth.clienting.audio.loadSounds
import marloth.clienting.audio.newMusicPlayer
import marloth.clienting.gui.hud.cooldownMeshKey
import marloth.clienting.gui.hud.createCooldownCircleMesh
import marloth.clienting.rendering.createMeshes
import marloth.clienting.rendering.loadingImages
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.drawing.setGlobalFonts
import silentorb.mythic.glowing.Glow
import silentorb.mythic.glowing.TextureFormat
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.meshes.VertexSchemas
import silentorb.mythic.lookinglass.meshes.createVertexSchemas
import silentorb.mythic.platforming.ImageInfo
import silentorb.mythic.platforming.ImageLoader
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.resource_loading.scanResources
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.typography.FontSet
import java.nio.file.Path

fun scanTextureResources(rootPath: String): List<Path> =
    scanResources(rootPath, listOf(".jpg", ".png"))

fun gatherTextures(loadImage: ImageLoader, attributes: TextureAttributeMapper): List<DeferredTexture> =
    scanTextureResources("models")
        .plus(scanTextureResources("textures"))
        .plus(scanTextureResources("images"))
        .map { deferImageFile(loadImage, it, attributes(it)) }

fun loadPngImageInfo(display: PlatformDisplay, path: String): ImageInfo {
  val info = display.loadImageInfo(path)
//  if (info.channels == 1)
//    throw Error("Marloth does not currently support single channel images: ${path}")
  return info
}

fun gatherTextures(display: PlatformDisplay, displayOptions: DisplayOptions): List<DeferredTexture> {
  val defaultAttributes = textureAttributesFromConfig(displayOptions)
  val transparentImageAttributes = defaultAttributes.copy(
      format = TextureFormat.rgba,
  )
  val grayscaleImageAttributes = defaultAttributes.copy(
      format = TextureFormat.scalar,
  )
  return gatherTextures(display.loadImage) { path ->
    if (path.toString().contains(".png")) {
      val info = loadPngImageInfo(display, path.toString())
      when (info.channels) {
        1 -> grayscaleImageAttributes
        4 -> transparentImageAttributes
        else -> defaultAttributes
      }
    } else
      defaultAttributes
  }
}

fun newRenderer(
    dimensions: Vector2i,
    options: DisplayOptions,
    fontSource: () -> List<FontSet>
): Renderer {
  val glow = Glow()
  if (getDebugBoolean("DEBUG_OPENGL")) {
    registerGlDebugLogging()
  }
  glow.state.clearColor = Vector4(0f, 0f, 0f, 1f)
  val vertexSchemas: VertexSchemas = createVertexSchemas()
  val (loadedMeshes, loadedArmatures) = createMeshes(vertexSchemas)
  val meshes = loadedMeshes
  val armatures = loadedArmatures.associateBy { it.id }
  val multisampler = if (options.multisamples == 0)
    null
  else
    createMultiSampler(glow, dimensions.x, dimensions.y, options.multisamples)

  return Renderer(
      glow = glow,
      options = options,
      fonts = fontSource(),
      meshes = meshes.toMutableMap(),
      armatures = armatures,
      vertexSchemas = vertexSchemas,
      multisampler = multisampler,
  )
}

fun newClient(platform: Platform, displayOptions: DisplayOptions): Client {
  val initialDeferredTextures = gatherTextures(platform.display, displayOptions)
  val windowInfo = platform.display.getInfo()
  val renderer = newRenderer(
      dimensions = windowInfo.dimensions,
      options = displayOptions,
      fontSource = ::gatherFontSets
  )
  val (immediateTextures, deferredTextures) = initialDeferredTextures
      .partition { loadingImages.contains(it.name) }

  renderer.textures += texturesToGpu(loadDeferredTextures(immediateTextures))

  setGlobalFonts(renderer.fonts)
  platform.audio.start(50)
  val soundLibrary = loadSounds(platform.audio)
  val music = newMusicPlayer()
//  music?.start()

  return Client(
      platform = platform,
      renderer = renderer,
      soundLibrary = soundLibrary,
      textureLoadingState = TextureLoadingState(deferredTextures),
      customBloomResources = mapOf(
          cooldownMeshKey to createCooldownCircleMesh(),
          canvasRendererKey to renderer
      ),
      resourceInfo = ResourceInfo(
          meshShapes = getMeshShapes(renderer),
          textures = getClientTextures(renderer, deferredTextures),
      ),
      music = music,
  )
}
