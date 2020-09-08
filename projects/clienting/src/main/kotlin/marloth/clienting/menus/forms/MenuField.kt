package marloth.clienting.menus.forms

import marloth.clienting.menus.MenuItemFlower
import marloth.clienting.menus.TextStyles
import marloth.clienting.menus.localizedLabel
import marloth.clienting.menus.menuButtonWrapper
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.Flower
import silentorb.mythic.bloom.horizontalList

fun menuField(labelText: Text, flower: Flower): MenuItemFlower =
    menuButtonWrapper {
      horizontalList(20)(
          listOf(
              localizedLabel(TextStyles.mediumBlack, labelText),
              flower
          )
      )
    }
