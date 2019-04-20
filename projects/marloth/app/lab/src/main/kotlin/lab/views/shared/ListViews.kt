package lab.views.shared

import lab.views.model.SelectableListType
import lab.views.model.SelectionEvent
import marloth.clienting.gui.TextStyles
import mythic.bloom.*

import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.glowing.globalState
import mythic.spatial.Vector4
import mythic.spatial.toVector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.calculateTextDimensions
import mythic.typography.resolveTextStyle
import org.joml.Vector2i


fun drawListItem(text: String, isSelected: Boolean): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (isSelected)
    Pair(TextStyles.smallBlack, LineStyle(Vector4(1f), 2f))
  else
    Pair(TextStyles.smallBlack, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

  drawBorder(bounds, canvas, style.second)

  val textConfig = TextConfiguration(text, bounds.position.toVector2(), resolveTextStyle(canvas.fonts, style.first))
  val textDimensions = calculateTextDimensions(textConfig).toVector2i()
  val centered = centeredPosition(bounds, textDimensions)
  val position = Vector2i(bounds.position.x + 10, centered.y)
  canvas.drawText(position, style.first, text)
}

data class SelectableItem(
    val name: String,
    val isSelected: Boolean
)

typealias SelectionResult = Pair<List<FlatBox>, List<ClickBox<SelectionEvent>>>

fun drawSelectableList(items: List<SelectableItem>, list: SelectableListType, bounds: Bounds): SelectionResult {
  val padding = 10
  val itemHeight = 30

  val partialBoxes = items
      .map {
        assert(it.name != "")
        PartialBox(itemHeight, drawListItem(it.name, it.isSelected))
      }

//  listOld(
//      children(verticalFromLengths(15), nodeRow)

  val buttonBoxes = arrangeListComplex(lengthArranger(verticalPlane, padding), partialBoxes, bounds)
  val boxes = listOf<FlatBox>(
//      Box(bounds, drawSidePanel())
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
