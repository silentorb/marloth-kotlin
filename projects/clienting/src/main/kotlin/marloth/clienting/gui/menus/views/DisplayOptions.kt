package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.gui.menus.MenuItem
import marloth.clienting.gui.menus.menuFlower
import marloth.clienting.gui.menus.forms.menuField
import marloth.clienting.gui.menus.forms.spinField
import marloth.scenery.enums.Text
import silentorb.mythic.lookinglass.DisplayOptions
import silentorb.mythic.platforming.WindowMode

fun windowModeTextMap(windowMode: WindowMode): Text=
    when(windowMode){
      WindowMode.fullscreen->Text.gui_fullscreen
      WindowMode.windowed->Text.gui_windowed
      WindowMode.windowedFullscreen->Text.gui_windowedFullscreen
    }

fun displayOptionsFlower(display: DisplayOptions): StateFlower = { definitions, state ->
  menuFlower(Text.gui_displayOptions, listOf(
      MenuItem(
          flower = menuField(definitions.textLibrary(Text.gui_windowMode),
              spinField(WindowMode.values().toList(), display.windowMode, definitions.textLibrary(windowModeTextMap(display.windowMode)))
          )
      ),
//        MenuItem(
//            flower = localizedLabel(textStyles.smallBlack, Text.gui_resolution)
//        )
  ))(definitions, state)
}
