package marloth.clienting.gui

import intellect.acessment.lightRating
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.ent.Id
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.IndexedTextStyle
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i
import simulation.World
import java.text.DecimalFormat

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
  val textConfig = TextConfiguration(state.text, bounds.position.toVector2(), blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val position = centeredPosition(bounds, textDimensions.toVector2i())
  canvas.drawText(position, blackStyle, state.text)
}

fun menuLayout(bounds: Bounds, state: MenuState): Boxes {
  val buttonHeight = 50
  val items = listOf(
      "New Game",
      "Continue Game",
      "Quit"
  ).mapIndexed { index, it -> PartialBox(buttonHeight, drawMenuButton(ButtonState(it, state.focusIndex == index))) }

  val itemLengths = items.map { it.length }
  val menuHeight = listContentLength(10, itemLengths)
  val menuBounds = centeredBounds(bounds, Vector2i(200, menuHeight))
  val menuPadding = 10

  return listOf(Box(menuBounds, menuBackground))
      .plus(arrangeListComplex(lengthArranger(vertical, menuPadding), items, menuBounds))
}

private val textStyle = IndexedTextStyle(0, 12f, grayTone(0.7f))

fun characterHealth(world: World, id: Id): String {
  val resource = world.deck.characters[id]!!.health
  val value = resource.value
  val max = resource.max
  return "$value / $max"
}

val df = DecimalFormat("#0.00")

fun characterVisibility(world: World, id: Id): String {
  val rating = lightRating(world, id)
  return df.format(rating)
}

fun hudLayout(world: World): Flower {
  val player = world.players.first().id
  val rows = listOf(
      label(textStyle, characterHealth(world, player)),
      label(textStyle, characterVisibility(world, player)),
      label(textStyle, df.format(world.deck.bodies[player]!!.velocity.length()))
  )
  return offset(Vector2i(10))(
      list(vertical, 10)(rows)
  )
}

fun renderGui(bounds: Bounds, canvas: Canvas, world: World, state: MenuState) {
  val hudBoxes = hudLayout(world)(Seed(bounds = bounds))
  renderLayout(hudBoxes, canvas)

  if (!state.isVisible)
    return

  renderLayout(menuLayout(bounds, state), canvas)
}
