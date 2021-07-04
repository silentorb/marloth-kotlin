package marloth.clienting.gui.menus.views.main

import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.menus.TextStyles
import silentorb.mythic.bloom.label

fun devView(): StateFlower = { _, _ ->
  { _ ->
    label(TextStyles.smallWhite, "a")
  }
}
