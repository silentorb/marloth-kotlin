package marloth.clienting.gui.menus.views.main

import marloth.clienting.ClientEventType
import marloth.clienting.ClientState
import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.forms.clientEventSpinHandlers
import marloth.clienting.gui.menus.general.forms.menuField
import marloth.clienting.gui.menus.general.forms.spinField
import marloth.clienting.gui.menus.general.MenuItem
import marloth.clienting.gui.menus.general.MenuItemFlower
import marloth.clienting.gui.menus.general.menuFlower
import marloth.scenery.enums.Text
import marloth.scenery.enums.TextId
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.bloom.emptyBox
import silentorb.mythic.bloom.label
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.platforming.DisplayMode
import silentorb.mythic.platforming.WindowMode
import silentorb.mythic.spatial.Vector2i

fun windowModeTextMap(windowMode: WindowMode): Text =
    when (windowMode) {
      WindowMode.fullscreen -> TextId.gui_fullscreen
      WindowMode.windowed -> TextId.gui_windowed
      WindowMode.windowedFullscreen -> TextId.gui_windowedFullscreen
    }

fun formatResolutionOption(resolution: Vector2i?): String =
    if (resolution == null)
      "Auto"
    else
      "${resolution.x} x ${resolution.y}".padEnd(11)

fun windowModeField(textLibrary: TextResourceMapper, windowMode: WindowMode): MenuItem {
  val windowModeOptions = WindowMode.values().toList()
  return MenuItem(
      flower = menuField(textLibrary(TextId.gui_windowMode),
          spinField(textLibrary(windowModeTextMap(windowMode)),
              clientEventSpinHandlers(ClientEventType.setStagingWindowMode, windowModeOptions, windowMode)
          )
      )
  )
}

fun resolutionField(textLibrary: TextResourceMapper, displayModes: List<DisplayMode>, options: DisplayOptions): MenuItem {
  val resolution = if (options.windowMode == WindowMode.windowed)
    options.windowedResolution
  else
    options.fullscreenResolution

  val setResolutionEventType = if (options.windowMode == WindowMode.windowed)
    ClientEventType.setStagingWindowedResolution
  else
    ClientEventType.setStagingFullscreenResolution

  val officialResolutionOptions = displayModes
      .map { it.resolution }
      .distinct()

  val resolutionOptions = if (!officialResolutionOptions.contains(resolution))
    listOf(resolution) + officialResolutionOptions
  else
    officialResolutionOptions

  val valueEditor: MenuItemFlower = if (options.windowMode != WindowMode.windowedFullscreen)
    spinField(
        formatResolutionOption(resolution),
        clientEventSpinHandlers(setResolutionEventType, resolutionOptions, resolution)
    )
  else
    { _ -> label(TextStyles.mediumBlack, "-") }

  return MenuItem(
      flower = menuField(textLibrary(TextId.gui_resolution),
          valueEditor
      )
  )
}

fun displayOptionsFlower(clientState: ClientState): StateFlowerTransform =
    dialogWrapperWithExtras { definitions, state ->
      val textLibrary = definitions.textLibrary
      val options = state.displayChange?.options
      if (options == null)
        emptyBox
      else {
        menuFlower(TextId.gui_displayOptions, listOf(
            windowModeField(textLibrary, options.windowMode),
            resolutionField(textLibrary, clientState.displayModes, options),
        ))(definitions, state)
      }
    }
