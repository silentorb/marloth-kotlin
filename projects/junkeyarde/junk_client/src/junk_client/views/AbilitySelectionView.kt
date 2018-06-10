package junk_client.views

import junk_client.*
import junk_simulation.AbilityType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone
import mythic.spatial.Vector2

fun listItemDepiction(content: String): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = Pair(12f, LineStyle(black, 1f))
  drawBorder(bounds, canvas, style.second)
  drawCenteredText(canvas, black, content, bounds)
}

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

fun abilitySelectionView(state: AbilitySelectionState, bounds: Bounds): Layout {
  val rowLengths = resolveLengths(bounds.dimensions.y, listOf(itemHeight, null, itemHeight))
  val rows = listBounds(verticalPlane, Vector2(), bounds, rowLengths)

  val columnBounds = splitBoundsHorizontal(rows[1])
  return listOf(label(remainingPoints(state).toString(), rows[0]))
      .plus(abilitySelectionList(AbilitySelectionColumn.available, state.available, columnBounds.first))
      .plus(abilitySelectionList(AbilitySelectionColumn.selected, state.selected, columnBounds.second))
      .plus(button("OK", CommandType.submit, rows[2]))
}
