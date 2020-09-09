package marloth.clienting.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.menus.*
import marloth.clienting.menus.ButtonState
import silentorb.mythic.bloom.*
import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector2i
import marloth.scenery.enums.Text
import simulation.entities.AttachmentCategory
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
import simulation.misc.Definitions

fun drawWareButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  val background = if (state.isEnabled)
    grayTone(0.5f)
  else
    grayTone(0.25f)

  drawFill(bounds, canvas, background)
  drawMenuButtonBorder(state.hasFocus, bounds, canvas)
}

fun wareFlower(content: String, isEnabled: Boolean): MenuItemFlower = { hasFocus ->
  { seed: Seed ->
    Box(
        bounds = Bounds(
            dimensions = Vector2i(300, 50)
        ),
        depiction = drawWareButton(
            ButtonState(
                text = content,
                hasFocus = hasFocus,
                isEnabled = isEnabled)
        )
    )
  }
}

fun wareMenuItem(definitions: Definitions, deck: Deck, merchant: Id,
                 player: Id, customerMoney: Int, id: Id): MenuItem {
  val ware = deck.wares[id]!!
  val definition = definitions.accessories[ware.type]!!
  val name = definitions.textLibrary(definition.name)
  val price = ware.price
  val canPurchase = ware.price <= customerMoney
  val event = if (canPurchase)
    ClientOrServerEvent(
        server = PurchaseEvent(
            customer = player,
            merchant = merchant,
            ware = id,
            wareType = ware.type
        )
    )
  else
    null

  return MenuItem(
      flower = wareFlower("$name $$price", canPurchase),
      event = event
  )
}

fun merchantInfoFlower(customerMoney: Int) =
    div(
        name = "merchant",
        forward = forwardDimensions(width = fixed(200)),
        reverse = shrink
    )(
        forwardMargin(20)(
            list(verticalPlane, 10)(listOf(
                label(TextStyles.smallBlack, "Money: $$customerMoney")
            ))
        )
    )

fun merchantView(deck: Deck, player: Id): StateFlower = { definitions, state ->
  val merchant = getPlayerInteractingWith(deck, player)!!
  val customerMoney = deck.characters[player]!!.money
  val buttons = deck.attachments
      .filter { it.value.target == merchant && it.value.category == AttachmentCategory.inventory }
      .map { (id, _) ->
        wareMenuItem(definitions, deck, merchant, player, customerMoney, id)
      }

  dialog(Text.gui_merchant)(
      list(horizontalPlane, 10)(listOf(
          menuFlower(buttons, state.menuFocusIndex),
          merchantInfoFlower(customerMoney)
      ))
  )
}
