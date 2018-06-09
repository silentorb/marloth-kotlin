package junk_client

import junk_simulation.AbilityType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.platforming.Platform
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.Vector2i

fun listItemDepiction(text: String): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = Pair(12f, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val blackStyle = TextStyle(canvas.fonts[0], style.first, Vector4(0f, 0f, 0f, 1f))
  val textConfig = TextConfiguration(text, bounds.position, blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val centered = centeredPosition(bounds, textDimensions)
  val position = Vector2(bounds.position.x + 10f, centered.y)
  canvas.drawText(text, position, blackStyle)
}

fun abilitySelectionList(abilities: List<AbilityType>, bounds: Bounds): Layout {
  val padding = Vector2(10f)
  val itemHeight = 30f
  val partialBoxes = abilities
      .map { PartialBox(itemHeight, listItemDepiction(it.name)) }

  return arrangeList(verticalArrangement(padding), partialBoxes, bounds)
}

fun abilitySelectionView(state: AbilitySelectionState, bounds: Bounds): Layout {
  val columnBounds = splitBoundsHorizontal(bounds)
  return abilitySelectionList(state.available, columnBounds.first)
      .plus(abilitySelectionList(state.selected, columnBounds.second))
}

class Client(val platform: Platform) {
  val renderer: Renderer = Renderer()

  fun getWindowInfo() = platform.display.getInfo()

  fun update(state: AppState, delta: Float): AppState {
    val windowInfo = getWindowInfo().copy(dimensions = Vector2i(320, 200))
    val canvas = createCanvas(windowInfo)
    val bounds = Bounds(dimensions = windowInfo.dimensions.toVector2())
    val layout = abilitySelectionView(state.abilitySelectionState!!, bounds)
    renderScreen(renderer, layout, canvas, windowInfo, getWindowInfo())
    return state
  }
}
