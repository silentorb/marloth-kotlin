package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.scenery.enums.Text
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.bloom.*
import silentorb.mythic.ent.Id
import simulation.entities.Ware
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
import simulation.misc.Definitions

fun wareMenuItem(definitions: Definitions, merchant: Id, player: Id,
                 customerMoney: Int, id: Id, ware: Ware): MenuItem {
  val definition = definitions.accessories[ware.type]!!
  val name = definitions.textLibrary(definition.name)
  val price = ware.price
  val canPurchase = ware.price <= customerMoney
  val events = if (canPurchase)
    listOf(
        PurchaseEvent(
            customer = player,
            merchant = merchant,
            ware = id,
            wareType = ware.type
        )
    )
  else
    listOf()

  return MenuItem(
      flower = menuTextFlower("$name $$price", canPurchase),
      events = events
  )
}

fun moneyLabel(textLibrary: TextResourceMapper, value: Int): Box =
    label(TextStyles.smallBlack, "${textLibrary(Text.gui_money)}: $$value")

fun merchantInfoFlower(textLibrary: TextResourceMapper, customerMoney: Int): Box =
    moneyLabel(textLibrary, customerMoney)

fun merchantView(deck: Deck, player: Id): StateFlowerTransform = dialogWrapper { definitions, state ->
  val merchant = getPlayerInteractingWith(deck, player)
  if (merchant == null)
    emptyBox
  else {
    val customerMoney = deck.characters[player]?.money ?: 0
    val wares = deck.vendors[merchant]?.wares ?: mapOf()
    val menu = wares
        .map { (id, ware) ->
          wareMenuItem(definitions, merchant, player, customerMoney, id, ware)
        }

    dialog(definitions, Text.gui_merchant,
        boxList2(
            horizontalPlane, 10,
            menuFlower(menu, state.menuFocusIndex, 100),
            merchantInfoFlower(definitions.textLibrary, customerMoney),
        )
    )

  }
}
