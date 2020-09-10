package marloth.clienting.menus.forms

import marloth.clienting.menus.MenuItemFlower
import marloth.clienting.menus.TextStyles
import marloth.clienting.menus.menuButtonWrapper
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.horizontalList
import silentorb.mythic.bloom.label

fun menuField(labelText: String, flower: Box): MenuItemFlower =
    menuButtonWrapper {
      horizontalList(20)(
          listOf(
              label(TextStyles.mediumBlack, labelText),
              flower
          )
      )
    }
