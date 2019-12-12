package compuquest_client.views

import compuquest_client.*
import compuquest_simulation.AbilityType
import compuquest_simulation.CommandType
import mythic.bloom.*
import mythic.bloom.next.Flower
import mythic.bloom.next.emptyFlower
import mythic.drawing.Canvas
import mythic.drawing.grayTone

fun listItemDepiction(content: String): Depiction = { bounds: Bounds, canvas: Canvas ->
  drawFill(bounds, canvas, grayTone(0.5f))
  val style = Pair(12f, LineStyle(black, 1f))
  drawBorder(bounds, canvas, style.second)
//  drawCenteredText(canvas, black, content, bounds)
}

fun abilitySelectionList(column: ShopColumn, abilities: List<AbilityType>): Flower {
//  val rows = listBounds(verticalPlane, standardPadding, bounds, abilities.map { itemHeight })
  return list(verticalPlane)(abilities.map { a ->
    depict(listItemDepiction(a.name)) //plusLogic ShopSelectionEvent(column, a)
  })
}

fun shopView(state: ShopState): Flower {
//  val rowLengths = resolveLengths(bounds.dimensions.y, listOf(itemHeight, null, itemHeight))
//  val rows = listBounds(verticalPlane, 0f, bounds, rowLengths)

//  val columnBounds = arrangeHorizontal(standardPadding, rows[1], listOf(150f, null, 150f))
  return list(horizontalPlane, 10)(listOf(
      label(textStyles.smallWhite, remainingPoints(state).toString()),
      abilitySelectionList(ShopColumn.available, state.available),
      abilitySelectionList(ShopColumn.selected, state.selected),
      if (state.existing.plus(state.selected).any())
        button("OK", CommandType.submit)
      else
        emptyFlower
  ))
}
