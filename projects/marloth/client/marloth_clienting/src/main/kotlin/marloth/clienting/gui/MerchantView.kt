package marloth.clienting.gui

import mythic.bloom.*
import mythic.bloom.next.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.calculateTextDimensions
import mythic.typography.resolveTextStyle
import org.joml.Vector2i
import simulation.entities.AttachmentTypeId
import simulation.main.Deck

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

fun wareFlower(content: String, index: Int): Flower = { seed: Seed ->
  Box(
      bounds = Bounds(
          dimensions = Vector2i(300, 50)
      ),
      depiction = drawWareButton(
          ButtonState(content, menuFocusIndex(seed.bag) == index)
      ),
      name = "menu button"
  )
}

fun merchantView(textResources: TextResources, deck: Deck): Flower {
  val merchant = getPlayerInteractingWith(deck)!!
  val buttons = deck.attachments
      .filter { it.value.target == merchant && it.value.category == AttachmentTypeId.inventory }
      .map { (id, _) ->
        val entity = deck.entities[id]!!
        val ware = deck.wares[id]!!
        wareFlower("${entity.type} $${ware.price}", 0)
      }

  val menuBox = div(
      reverse = reverseOffset(left = centered, top = centered) + shrink,
      depiction = menuBackground,
      logic = menuNavigationLogic
  )

  val gap = 20

  return menuBox(
      (margin(all = gap))(
          (list(verticalPlane, gap))(buttons)
      )
  )
}
