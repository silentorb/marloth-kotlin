package junk_client.views

import mythic.bloom.*
import mythic.drawing.Canvas
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import mythic.typography.TextConfiguration
import mythic.typography.TextStyle
import mythic.typography.calculateTextDimensions
import org.joml.plus

val itemHeight = 15f
val standardPadding = 5f

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

fun label(color: Color, content: String, bounds: Bounds) =
    Box(bounds = bounds, depiction = { b: Bounds, canvas: Canvas ->
      drawText(canvas, color, content, b.position + standardPadding)
    })

fun button(content: String, handler: Any, bounds: Bounds): Box {
  return Box(
      bounds = bounds,
      depiction = listItemDepiction(content),
      handler = handler
  )
}

fun <T> verticalList(items: List<T>, bounds: Bounds, itemHeight: Float, padding: Float, boxer: (T, Bounds) -> List<Box>): LayoutOld {
  val rows = listBounds(verticalPlane, padding, bounds, items.map { itemHeight })
  return items
      .zip(rows, { a, b -> boxer(a, b) })
      .flatten()
}