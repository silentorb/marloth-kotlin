package marloth.clienting.gui

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.plus
import rendering.SceneRenderer

data class ButtonState(
    val text: String,
    val hasFocus: Boolean
)

fun depictBackground(backgroundColor: Vector4): Depiction = { b: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(b, canvas, backgroundColor)
  drawBorder(b, canvas, Vector4(0f, 0f, 0f, 1f))
}

val menuBackground: Depiction = depictBackground(grayTone(0.5f))

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
  val textConfig = TextConfiguration(state.text, bounds.position, blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions)
  canvas.drawText(state.text, position, blackStyle)
}

fun createMenuLayout(bounds: Bounds, state: MenuState): List<Box> {
  val buttonHeight = 50f
  val items = listOf(
      "New Game",
      "Continue Game",
      "Quit"
  ).mapIndexed { index, it -> PartialBox(buttonHeight, drawMenuButton(ButtonState(it, state.focusIndex == index))) }

  val itemLengths = items.map { it.length }
  val menuHeight = listContentLength(10f, itemLengths)
  val menuBounds = centeredBounds(bounds, Vector2(200f, menuHeight))
  val menuPadding = 10f

  return listOf(Box(menuBounds, menuBackground))
      .plus(arrangeListComplex(arrangeVertical(menuPadding), items, menuBounds))
}

fun renderMenus(bounds: Bounds, canvas: Canvas, state: MenuState) {
  val layout = createMenuLayout(bounds, state)
  renderLayout(layout, canvas)
}

fun renderGui(renderer: SceneRenderer, bounds: Bounds, canvas: Canvas, state: MenuState) {
  canvas.drawText(TextConfiguration("Testing", bounds.position + Vector2(10f, 10f),
      TextStyle(canvas.fonts[0], 12f, Vector4(1f))))

  if (state.isVisible)
    renderMenus(bounds, canvas, state)
}
