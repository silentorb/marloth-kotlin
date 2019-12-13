package compuquest_client.views

import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.depict
import silentorb.mythic.bloom.next.Flower

val itemHeight = 15f
val standardPadding = 5f

//fun drawText(canvas: Canvas, color: Vector4, content: String, position: Vector2) {
//  val style = TextStyle(canvas.fonts[0], 1f, color)
//  canvas.drawText(content, position, style)
//}

//fun drawCenteredText(canvas: Canvas, color: Vector4, content: String, bounds: Bounds) {
//  val style = TextStyle(canvas.fonts[0], 1f, color)
//  val textConfig = TextConfiguration(content, bounds.position, style)
//  val textDimensions = calculateTextDimensions(textConfig)
//  val centered = centeredPosition(bounds, textDimensions)
//  val centeredPosition = Vector2(bounds.position.x + 10f, centered.y)
//  canvas.drawText(content, centeredPosition, style)
//}

//fun label(color: Color, content: String, bounds: Bounds) =
//    Box(bounds = bounds, depiction = { b: Bounds, canvas: Canvas ->
//      drawText(canvas, color, content, b.position + standardPadding)
//    })

fun button(content: String, handler: Any): Flower {
  return depict(listItemDepiction(content))
//  return Box(
//      bounds = bounds,
//      depiction = listItemDepiction(content),
//      handler = handler
//  )
}