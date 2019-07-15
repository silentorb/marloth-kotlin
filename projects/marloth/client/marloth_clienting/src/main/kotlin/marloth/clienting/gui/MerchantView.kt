package marloth.clienting.gui

import mythic.bloom.*
import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import mythic.bloom.next.Seed
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
import simulation.entities.AttachmentTypeId
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
import simulation.misc.AccessoryDefinitions

fun drawWareButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (state.hasFocus)
    Pair(TextStyles.smallBlack, LineStyle(Vector4(1f), 2f))
  else
    Pair(TextStyles.smallBlack, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val textConfig = TextConfiguration(state.text, bounds.position.toVector2(), resolveTextStyle(canvas.fonts, style.first))
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions.toVector2i())
  canvas.drawText(position, style.first, state.text)
}

fun wareFlower(content: String): MenuItemFlower = { hasFocus ->
  { seed: Seed ->
    Box(
        bounds = Bounds(
            dimensions = Vector2i(300, 50)
        ),
        depiction = drawWareButton(
            ButtonState(content, hasFocus)
        ),
        name = "menu button"
    )
  }
}

fun wareMenuItem(textResources: TextResources, definitions: AccessoryDefinitions, deck: Deck, merchant: Id, player: Id,
                 id: Id): MenuItem {
  val ware = deck.wares[id]!!
  val definition = definitions[ware.type]!!
  val name = textResources[definition.name]!!
  val price = ware.price
  return MenuItem(
      flower = wareFlower("$name $$price"),
      event = GuiEvent(
          type = GuiEventType.gameEvent,
          data = PurchaseEvent(
              customer = player,
              merchant = merchant,
              ware = id
          )
      )
  )
}

fun merchantView(textResources: TextResources, definitions: AccessoryDefinitions, deck: Deck, player: Id): Flower {
  val merchant = getPlayerInteractingWith(deck)!!
  val buttons = deck.attachments
      .filter { it.value.target == merchant && it.value.category == AttachmentTypeId.inventory }
      .map { (id, _) ->
        wareMenuItem(textResources, definitions, deck, merchant, player, id)
      }

  return menuFlower(buttons)
}
