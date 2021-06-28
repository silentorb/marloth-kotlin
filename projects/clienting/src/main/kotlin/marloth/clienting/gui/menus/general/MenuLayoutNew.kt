package marloth.clienting.gui.menus.general

import marloth.clienting.gui.EventUnion
import silentorb.mythic.bloom.menuItemIndexKey
import marloth.clienting.gui.menus.logic.onActivateKey
import marloth.clienting.gui.menus.logic.onClickKey
import silentorb.mythic.bloom.*

val stretchyFieldWrapper: (Int, Box) -> Flower = { index, box ->
  { seed ->
    val gap = 20
    val wrapped = boxMargin(all = gap, top = 12)(
        box
    )
    centered(wrapped)(seed)
        .copy(
            depiction = drawMenuButtonBackground(index == getFocusIndex(seed.state))
        )
  }
}

val fieldWrapper: (Int, Flower) -> Flower = { index, flower ->
  { seed ->
    val box = flower(seed)
    if (index == getFocusIndex(seed.state)) {
      val gap = 0
      val wrapped = boxMargin(all = gap, right = 2)(
          box
      )
      wrapped
          .copy(
              depiction = drawMenuButtonBackground(true)
          )
    } else
      box
  }
}

fun addMenuItemInteractivity(index: Int, events: List<EventUnion>, flower: Flower): Flower = { seed ->
  val hasFocus = index == getFocusIndex(seed.state)
  val attributes = if (hasFocus)
    mapOf(onActivateKey to events, onClickKey to events)
  else
    mapOf()

  flower(seed)
      .addAttributes(attributes + (menuItemIndexKey to index))
}
