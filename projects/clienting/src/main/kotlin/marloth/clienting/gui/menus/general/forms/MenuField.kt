package marloth.clienting.gui.menus.general.forms

import marloth.clienting.gui.menus.general.MenuItemFlower
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.general.menuButtonWrapper
import silentorb.mythic.bloom.horizontalList
import silentorb.mythic.bloom.label

fun menuField(labelText: String, flower: MenuItemFlower): MenuItemFlower =
    menuButtonWrapper { hasFocus ->
      horizontalList(20)(
          listOf(
              label(TextStyles.mediumBlack, labelText),
              flower(hasFocus)
          )
      )
    }
