package marloth.clienting.gui

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i

fun drawMenuButton(state: ButtonState): Depiction = { bounds: Bounds, canvas: Canvas ->
  //  menuBackground(bounds, canvas)
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (state.hasFocus)
    Pair(12f, LineStyle(Vector4(1f), 2f))
  else
    Pair(12f, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val blackStyle = TextStyle(canvas.fonts[0], style.first, Vector4(0f, 0f, 0f, 1f))
  val textConfig = TextConfiguration(state.text, bounds.position.toVector2(), blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions.toVector2i())
  canvas.drawText(position, blackStyle, state.text)
}

fun menuLayout(textResources: TextResources, menu: Menu, bounds: Bounds, state: MenuState): Boxes {
  val buttonHeight = 50
  val items = menu
      .mapIndexed { index, it ->
        val content = textResources[it.text]!!
        PartialBox(buttonHeight, drawMenuButton(
            ButtonState(content, state.focusIndex == index)
        ))
      }

  val itemLengths = items.map { it.length }
  val menuHeight = listContentLength(10, itemLengths)
  val menuBounds = centeredBounds(bounds, Vector2i(200, menuHeight))
  val menuPadding = 10

  return listOf(Box(menuBounds, menuBackground))
      .plus(arrangeListComplex(lengthArranger(vertical, menuPadding), items, menuBounds))
}
