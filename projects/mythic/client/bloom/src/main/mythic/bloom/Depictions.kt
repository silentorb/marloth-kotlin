package mythic.bloom

import mythic.drawing.Canvas
import mythic.spatial.Vector4
import mythic.typography.TextStyle

data class DeferredTextStyle(
    val font: Int,
    val size: Float,
    val color: Vector4
)

typealias TextStyleSource = (Canvas) -> TextStyle

fun resolve(style: DeferredTextStyle):TextStyleSource = { c: Canvas ->
  TextStyle(
      font = c.fonts[style.font],
      size = style.size,
      color = style.color
  )
}

fun label(style: TextStyleSource, content: String): Depiction = { b, c ->
  val position = b.position
  c.drawText(position, style(c), content)
}