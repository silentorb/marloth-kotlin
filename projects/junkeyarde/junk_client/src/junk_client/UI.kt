package junk_client

import junk_simulation.AbilityType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.plus

fun drawText(canvas: Canvas, color: Vector4, content: String, position: Vector2) {
  val style = TextStyle(canvas.fonts[0], 1f, color)
  canvas.drawText(content, position, style)
}

fun drawCenteredText(canvas: Canvas, color: Vector4, content: String, bounds: Bounds) {
  val style = TextStyle(canvas.fonts[0], 1f, color)
  val textConfig = TextConfiguration(content, bounds.position, style)
  val textDimensions = calculateTextDimensions(textConfig)
  val centered = centeredPosition(bounds, textDimensions)
  val centeredPosition = Vector2(bounds.position.x + 10f, centered.y)
  canvas.drawText(content, centeredPosition, style)
}

fun listItemDepiction(content: String): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = Pair(12f, LineStyle(black, 1f))
  drawBorder(bounds, canvas, style.second)
  drawCenteredText(canvas, black, content, bounds)
}

private val itemHeight = 15f
val standardPadding = Vector2(5f, 5f)

fun abilitySelectionList(column: AbilitySelectionColumn, abilities: List<AbilityType>, bounds: Bounds): Layout {
  val rows = listBounds(verticalPlane, standardPadding, bounds, abilities.map { itemHeight })
  return abilities.zip(rows, { a, b ->
    Box(
        bounds = b,
        depiction = listItemDepiction(a.name),
        handler = AbilitySelectionEvent(column, a)
    )
  })
}

fun label(content: String, bounds: Bounds) =
    Box(bounds = bounds, depiction = { b: Bounds, canvas: Canvas ->
      drawText(canvas, white, content, b.position + standardPadding)
    })

fun abilitySelectionView(state: AbilitySelectionState, bounds: Bounds): Layout {
  val rowLengths = resolveLengths(bounds.dimensions.y, listOf(itemHeight, null, itemHeight))
  val rows = listBounds(verticalPlane, Vector2(), bounds, rowLengths)

  val columnBounds = splitBoundsHorizontal(rows[1])
  return listOf(label(remainingPoints(state).toString(), rows[0]))
      .plus(abilitySelectionList(AbilitySelectionColumn.available, state.available, columnBounds.first))
      .plus(abilitySelectionList(AbilitySelectionColumn.selected, state.selected, columnBounds.second))
}
