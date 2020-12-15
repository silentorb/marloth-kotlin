package marloth.clienting.gui.menus.views.interaction

import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.general.MenuItem
import marloth.clienting.gui.menus.general.menuFlower
import marloth.clienting.gui.menus.general.menuTextFlower
import marloth.scenery.enums.TextId
import marloth.scenery.enums.TextResourceMapper
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.boxList2
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.bloom.label
import silentorb.mythic.ent.Id
import simulation.entities.Ware
import simulation.happenings.PurchaseEvent
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
    label(TextStyles.smallBlack, "${textLibrary(TextId.gui_money)}: $$value")

fun merchantInfoFlower(textLibrary: TextResourceMapper, customerMoney: Int): Box =
    moneyLabel(textLibrary, customerMoney)

val merchantView = conversationPage { deck, player, other, character, otherCharacter ->
  { definitions, state ->
    val customerMoney = character.money
    val menu = otherCharacter.wares
        .map { (id, ware) ->
          wareMenuItem(definitions, other, player, customerMoney, id, ware)
        }

    boxList2(
        horizontalPlane, 10,
        menuFlower(menu, state.menuFocusIndex, 100),
        merchantInfoFlower(definitions.textLibrary, customerMoney),
    )
  }
}
  
