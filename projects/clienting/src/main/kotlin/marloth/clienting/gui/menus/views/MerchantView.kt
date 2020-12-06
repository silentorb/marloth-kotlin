package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.ButtonState
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import marloth.scenery.enums.Text
import simulation.entities.Ware
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
import simulation.misc.Definitions

//fun drawWareButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
//  val background = if (state.isEnabled)
//    grayTone(0.5f)
//  else
//    grayTone(0.25f)
//
//  drawFill(bounds, canvas, background)
//  drawMenuButtonBorder(state.hasFocus,, bounds, canvas)
//}

//fun wareFlower(content: String, isEnabled: Boolean): MenuItemFlower = { hasFocus ->
//  Box(
//      bounds = Bounds(
//          dimensions = Vector2i(300, 50)
//      ),
//      depiction = drawWareButton(
//          ButtonState(
//              text = content,
//              hasFocus = hasFocus,
//              isEnabled = isEnabled)
//      )
//  )
//}

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

//fun merchantInfoFlower(customerMoney: Int) =
//    div(
//        name = "merchant",
//        forward = forwardDimensions(width = fixed(200)),
//        reverse = shrink
//    )(
//        boxToFlower(
//            reverseMargin(20)(
//                boxList(verticalPlane, 10)(listOf(
//                    label(TextStyles.smallBlack, "Money: $$customerMoney")
//                ))
//            )
//        )
//    )

fun merchantView(deck: Deck, player: Id): StateFlower = { definitions, state ->
  val merchant = getPlayerInteractingWith(deck, player)!!
//  val customerMoney = deck.characters[player]!!.money
  val customerMoney = 0
  val wares = deck.merchants[merchant]?.wares ?: mapOf()
  val buttons = wares
      .map { (id, ware) ->
        wareMenuItem(definitions, merchant, player, customerMoney, id, ware)
      }

  dialog(definitions.textLibrary(Text.gui_merchant))(
      boxList(horizontalPlane, 10)(
          listOf(
//              menuFlower(buttons, state.menuFocusIndex),
              menuFlower(Text.gui_merchant, buttons)(definitions, state),
//              flowerToBox(merchantInfoFlower(customerMoney))
          )
      )
  )
}
