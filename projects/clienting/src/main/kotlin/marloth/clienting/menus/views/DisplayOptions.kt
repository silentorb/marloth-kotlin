package marloth.clienting.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.menus.MenuItem
import marloth.clienting.menus.localizedLabel
import marloth.clienting.menus.menuFlower
import marloth.clienting.menus.TextStyles
import marloth.clienting.menus.forms.menuField
import marloth.clienting.menus.forms.spinField
import marloth.scenery.enums.Text
import silentorb.mythic.lookinglass.DisplayConfig

fun displayOptionsFlower(display: DisplayConfig): StateFlower = { definitions, state ->
  menuFlower(Text.gui_displayOptions, listOf(
      MenuItem(
          flower = menuField(definitions.textLibrary(Text.gui_fullscreen), spinField("", "Fullscreen"))
      ),
//        MenuItem(
//            flower = localizedLabel(textStyles.smallBlack, Text.gui_resolution)
//        )
  ))(definitions, state)
}
