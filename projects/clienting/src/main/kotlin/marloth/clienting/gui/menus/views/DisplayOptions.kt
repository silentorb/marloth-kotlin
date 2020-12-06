package marloth.clienting.gui.menus.views

import marloth.clienting.ClientEventType
import marloth.clienting.ClientState
import marloth.clienting.StateFlower
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.forms.clientEventSpinHandlers
import marloth.clienting.gui.menus.forms.menuField
import marloth.clienting.gui.menus.forms.spinField
import marloth.scenery.enums.Text
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.bloom.emptyBox
import silentorb.mythic.bloom.label
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.platforming.DisplayMode
import silentorb.mythic.platforming.WindowMode
import silentorb.mythic.spatial.Vector2i

fun windowModeTextMap(windowMode: WindowMode): Text =
    when (windowMode) {
      WindowMode.fullscreen -> Text.gui_fullscreen
      WindowMode.windowed -> Text.gui_windowed
      WindowMode.windowedFullscreen -> Text.gui_windowedFullscreen
    }

fun formatResolutionOption(resolution: Vector2i): String =
    "${resolution.x} x ${resolution.y}".padEnd(11)

fun windowModeField(textLibrary: TextResourceMapper, windowMode: WindowMode): MenuItem {
  val windowModeOptions = WindowMode.values().toList()
  return MenuItem(
      flower = menuField(textLibrary(Text.gui_windowMode),
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
      flower = menuField(textLibrary(Text.gui_resolution),
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
        menuFlower(Text.gui_displayOptions, listOf(
            windowModeField(textLibrary, options.windowMode),
            resolutionField(textLibrary, clientState.displayModes, options),
        ))(definitions, state)
      }
    }
