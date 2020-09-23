package marloth.clienting.gui.menus.forms

import marloth.clienting.gui.menus.MenuItemFlower
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.menuButtonWrapper
import silentorb.mythic.bloom.Box
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
