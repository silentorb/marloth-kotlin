package marloth.clienting.gui

import mythic.bloom.next.Flower
import mythic.bloom.next.plus
import mythic.bloom.verticalPlane
import simulation.main.Deck
import simulation.misc.AttachmentTypeId

fun merchantView(textResources: TextResources, deck: Deck): Flower {
  val merchant = getPlayerInteractingWith(deck)!!
  val buttons = deck.attachments
      .filter { it.value.target == merchant && it.value.category == AttachmentTypeId.inventory }
      .map { (id, _) ->
        val entity = deck.entities[id]!!
        val ware = deck.wares[id]!!
        menuButton("${entity.type} $${ware.price}", 0)
      }

  val menuBox = mythic.bloom.next.div(
      reverse = mythic.bloom.next.reverseOffset(left = mythic.bloom.centered, top = mythic.bloom.centered) + mythic.bloom.next.shrink,
      depiction = menuBackground,
      logic = menuNavigationLogic
  )

  val gap = 20

  return menuBox(
      (mythic.bloom.next.margin(all = gap))(
          (mythic.bloom.list(verticalPlane, gap))(buttons)
      )
  )
}
