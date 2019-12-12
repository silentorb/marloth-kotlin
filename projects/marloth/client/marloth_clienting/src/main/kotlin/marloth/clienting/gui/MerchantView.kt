package marloth.clienting.gui

import mythic.bloom.*
import mythic.bloom.next.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.ent.Id
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.calculateTextDimensions
import mythic.typography.resolveTextStyle
import org.joml.Vector2i
import marloth.scenery.enums.Text
import simulation.entities.AttachmentCategory
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
import simulation.misc.AccessoryDefinitions

fun drawWareButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  val background = if (state.isEnabled)
    grayTone(0.5f)
  else
    grayTone(0.25f)

  drawFill(bounds, canvas, background)
  val style = if (state.hasFocus)
    Pair(textStyles.smallBlack, LineStyle(Vector4(1f), 2f))
  else
    Pair(textStyles.smallBlack, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val textConfig = TextConfiguration(state.text, bounds.position.toVector2(), resolveTextStyle(canvas.fonts, style.first))
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions.toVector2i())
  canvas.drawText(position, style.first, state.text)
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

fun wareMenuItem(textResources: TextResources, definitions: AccessoryDefinitions, deck: Deck, merchant: Id,
                 player: Id, customerMoney: Int, id: Id): MenuItem {
  val ware = deck.wares[id]!!
  val definition = definitions[ware.type]!!
  val name = textResources[definition.name]!!
  val price = ware.price
  val canPurchase = ware.price <= customerMoney
  val event = if (canPurchase)
    GuiEvent(
        type = GuiEventType.gameEvent,
        data = PurchaseEvent(
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
        margin(20)(
            list(verticalPlane, 10)(listOf(
                label(textStyles.smallBlack, "Money: $$customerMoney")
            ))
        )
    )

fun merchantView(textResources: TextResources, definitions: AccessoryDefinitions, deck: Deck, player: Id): Flower {
  val merchant = getPlayerInteractingWith(deck, player)!!
  val customerMoney = deck.characters[player]!!.money
  val buttons = deck.attachments
      .filter { it.value.target == merchant && it.value.category == AttachmentCategory.inventory }
      .map { (id, _) ->
        wareMenuItem(textResources, definitions, deck, merchant, player, customerMoney, id)
      }

  return dialog(Text.gui_merchant)(
      list(horizontalPlane, 10)(listOf(
          embeddedMenuFlower(buttons),
          merchantInfoFlower(customerMoney)
      ))
  )
}
