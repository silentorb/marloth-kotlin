package marloth.clienting

import marloth.clienting.gui.DeviceMode
import marloth.clienting.gui.GuiState
import marloth.clienting.gui.TextResources
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.baseFonts
import marloth.definition.texts.englishTextResources
import silentorb.mythic.aura.SoundLibrary
import silentorb.mythic.bloom.old.newBloomState
import silentorb.mythic.editing.closeImGui
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.Renderer
import silentorb.mythic.lookinglass.texturing.TextureLoadingState
import silentorb.mythic.platforming.Platform
import silentorb.mythic.typography.loadFontSets

const val maxPlayerCount = 4

fun newMarlothBloomState(primarydeviceMode: DeviceMode) =
    GuiState(
        bloom = newBloomState(),
        menuStack = listOf(),
        view = null,
        menuFocusIndex = 0,
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
    val customBloomResources: Map<String, Any>
) {
  fun getWindowInfo() = platform.display.getInfo()

  fun shutdown() {
    closeImGui()
    platform.display.shutdown()
    platform.audio.stop()
  }
}
