package marloth.clienting.gui

import mythic.bloom.next.Flower
import mythic.bloom.next.plus
import mythic.bloom.verticalPlane

fun merchantView(textResources: TextResources): Flower {
  val menuBox = mythic.bloom.next.div(
      reverse = mythic.bloom.next.reverseOffset(left = mythic.bloom.centered, top = mythic.bloom.centered) + mythic.bloom.next.shrink,
      depiction = menuBackground,
      logic = menuNavigationLogic
  )

  val gap = 20

  return menuBox(
      (mythic.bloom.next.margin(all = gap))(
          (mythic.bloom.list(verticalPlane, gap))(listOf(menuButton("Merchant", 0)))
      )
  )
}
