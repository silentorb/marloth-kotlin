package marloth.clienting

import marloth.clienting.audio.MusicPlayer
import marloth.clienting.gui.DeviceMode
import marloth.clienting.gui.GuiState
import marloth.clienting.gui.TextResources
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.baseFonts
import marloth.definition.texts.englishTextResources
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.editing.general.closeImGui
import silentorb.mythic.ent.Id
import silentorb.mythic.glowing.TextureAttributes
import silentorb.mythic.lookinglass.*
import silentorb.mythic.lookinglass.DeferredTexture
import silentorb.mythic.lookinglass.TextureLoadingState
import silentorb.mythic.platforming.Platform
import silentorb.mythic.typography.loadFontSets

const val maxPlayerCount = 4

fun defaultView(): ViewId? {
  val view = getDebugString("DEFAULT_VIEW")
  return if (view == null)
    null
  else
    ViewId.values().firstOrNull { it.name == view }
}

const val defaultMenuFocusIndex = 1

fun newGuiState(primarydeviceMode: DeviceMode) =
    GuiState(
        menuStack = listOf(),
        view = defaultView(),
        menuFocusIndex = defaultMenuFocusIndex,
        primarydeviceMode = primarydeviceMode,
    )

fun playerViews(client: ClientState): Map<Id, ViewId?> =
    client.guiStates.mapValues { it.value.view }

fun gatherFontSets() = loadFontSets(baseFonts, TextStyles)

data class Client(
    val platform: Platform,
    val renderer: Renderer,
    val soundLibrary: SoundLibrary,
    val textureLoadingState: TextureLoadingState,
    val textResources: TextResources = englishTextResources,
    val customBloomResources: Map<String, Any>,
    val resourceInfo: ResourceInfo,
    val music: MusicPlayer?,
) {
  fun getWindowInfo() = platform.display.getInfo()

  fun shutdown() {
    music?.stop()
    logGpuProfiling()
    closeImGui()
    platform.display.shutdown()
    platform.audio.stop()
  }
}

fun getClientTextures(renderer: Renderer, deferredTextures: List<DeferredTexture>): Map<String, TextureAttributes> =
    renderer.textures.mapValues { it.value.attributes } + deferredTextures.associate { it.name to it.attributes }
