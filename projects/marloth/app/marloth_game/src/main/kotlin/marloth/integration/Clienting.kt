package marloth.integration

import marloth.clienting.Client
import marloth.clienting.gatherFontSets
import silentorb.mythic.platforming.Platform
import silentorb.mythic.platforming.PlatformDisplay
import silentorb.mythic.lookinglass.DisplayConfig
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.shading.LightingConfig
import silentorb.mythic.lookinglass.textureAttributesFromConfig
import silentorb.mythic.lookinglass.texturing.DeferredTexture
import silentorb.mythic.lookinglass.texturing.gatherTextures
import silentorb.mythic.lookinglass.texturing.getFileShortName

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
      config = displayConfig,
      fontSource = ::gatherFontSets,
      lightingConfig = lightingConfig,
      textures = textures
  )
  return Client(platform, renderer)
}
