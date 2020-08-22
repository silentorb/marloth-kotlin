package lab.views.shared

import marloth.clienting.menus.textStyles
import silentorb.mythic.bloom.*

import silentorb.mythic.drawing.Canvas
import silentorb.mythic.drawing.grayTone
import silentorb.mythic.glowing.globalState
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.spatial.toVector2
import silentorb.mythic.spatial.toVector2i
import silentorb.mythic.typography.TextConfiguration
import silentorb.mythic.typography.calculateTextDimensions
import silentorb.mythic.typography.resolveTextStyle
import silentorb.mythic.spatial.Vector2i


fun drawListItem(text: String, isSelected: Boolean): Depiction = { bounds: Bounds, canvas: Canvas ->
  globalState.depthEnabled = false
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = if (isSelected)
    Pair(textStyles.mediumBlack, LineStyle(Vector4(1f), 2f))
  else
    Pair(textStyles.mediumBlack, LineStyle(Vector4(0f, 0f, 0f, 1f), 1f))

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

//typealias SelectionResult = Pair<List<FlatBox>, List<ClickBox<SelectionEvent>>>


//fun <T> drawSelectableEnumList(meshTypes: List<T>, selected: T, bounds: Bounds): SelectionResult
//    where T : Enum<T> {
//  val focusIndex = meshTypes.indexOf(selected)
//  val modelItems = meshTypes.mapIndexed { index, it ->
//    SelectableItem(it.name, focusIndex == index)
//  }
//
//  return drawSelectableList(modelItems, SelectableListType.model, bounds)
//}
