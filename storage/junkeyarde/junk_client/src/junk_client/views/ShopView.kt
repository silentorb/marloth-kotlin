package junk_client.views

import junk_client.*
import junk_simulation.AbilityType
import junk_simulation.CommandType
import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.drawing.grayTone

fun listItemDepiction(content: String): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = Pair(12f, LineStyle(black, 1f))
  drawBorder(bounds, canvas, style.second)
  drawCenteredText(canvas, black, content, bounds)
}

fun abilitySelectionList(column: ShopColumn, abilities: List<AbilityType>, bounds: Bounds): LayoutOld {
  val rows = listBounds(verticalPlane, standardPadding, bounds, abilities.map { itemHeight })
  return abilities.zip(rows, { a, b ->
    Box(
        bounds = b,
        depiction = listItemDepiction(a.name),
        handler = ShopSelectionEvent(column, a)
    )
  })
}

fun shopView(state: ShopState, bounds: Bounds): LayoutOld {
  val rowLengths = resolveLengths(bounds.dimensions.y, listOf(itemHeight, null, itemHeight))
  val rows = listBounds(verticalPlane, 0f, bounds, rowLengths)

  val columnBounds = arrangeHorizontal(standardPadding, rows[1], listOf(150f, null, 150f))
  return listOf(label(white, remainingPoints(state).toString(), rows[0]))
      .plus(abilitySelectionList(ShopColumn.available, state.available, columnBounds[0]))
      .plus(abilitySelectionList(ShopColumn.selected, state.selected, columnBounds[2]))
      .plus(if (state.existing.plus(state.selected).any())
        listOf(button("OK", CommandType.submit, rows[2]))
      else
        listOf()
      )
}
