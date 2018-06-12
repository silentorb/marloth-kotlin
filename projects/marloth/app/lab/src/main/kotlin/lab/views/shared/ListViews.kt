package lab.views.shared

import lab.views.model.SelectableListType
import lab.views.model.SelectionEvent
import lab.views.model.drawSidePanel
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions


fun drawListItem(text: String, isSelected: Boolean): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (isSelected)
    Pair(12f, LineStyle(Vector4(1f), 2f))
  else
    Pair(12f, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val blackStyle = TextStyle(canvas.fonts[0], style.first, Vector4(0f, 0f, 0f, 1f))
  val textConfig = TextConfiguration(text, bounds.position, blackStyle)
  val textDimensions = calculateTextDimensions(textConfig)
  val centered = centeredPosition(bounds, textDimensions)
  val position = Vector2(bounds.position.x + 10f, centered.y)
  canvas.drawText(text, position, blackStyle)
}

data class SelectableItem(
    val name: String,
    val isSelected: Boolean
)

typealias SelectionResult = Pair<List<Box>, List<ClickBox<SelectionEvent>>>

fun drawSelectableList(items: List<SelectableItem>, list: SelectableListType, bounds: Bounds): SelectionResult {
  val padding = 10f
  val itemHeight = 30f

  val partialBoxes = items
      .map { PartialBox(itemHeight, drawListItem(it.name, it.isSelected)) }

  val buttonBoxes = arrangeListComplex(arrangeVertical(padding), partialBoxes, bounds)
  val boxes = listOf(
      Box(bounds, drawSidePanel())
  )
      .plus(buttonBoxes)
  return Pair(boxes, buttonBoxes.mapIndexed { i, b -> ClickBox(b.bounds, SelectionEvent(list, i)) })
}

fun <T> drawSelectableEnumList(meshTypes: List<T>, selected: T, bounds: Bounds): SelectionResult
    where T : Enum<T> {
  val focusIndex = meshTypes.indexOf(selected)
  val modelItems = meshTypes.mapIndexed { index, it ->
    SelectableItem(it.name, focusIndex == index)
  }

  return drawSelectableList(modelItems, SelectableListType.model, bounds)
}
