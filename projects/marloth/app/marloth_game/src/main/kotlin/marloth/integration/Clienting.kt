package marloth.integration

import marloth.clienting.Client
import marloth.clienting.gatherFontSets
import mythic.platforming.Platform
import mythic.platforming.PlatformDisplay
import rendering.DisplayConfig
import rendering.Renderer
import rendering.shading.LightingConfig
import rendering.textureAttributesFromConfig
import rendering.texturing.DeferredTexture
import rendering.texturing.gatherTextures
import rendering.texturing.getFileShortName

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

fun newClient(platform: Platform, displayConfig: DisplayConfig, lightingConfig: LightingConfig = LightingConfig()): Client {
  val textures = gatherTextures(platform.display, displayConfig)
  val renderer = Renderer(
      textures = textures,
      display = platform.display,
      config = displayConfig,
      fontList = gatherFontSets(),
      lightingConfig = lightingConfig
  )
  return Client(platform, renderer)
}
