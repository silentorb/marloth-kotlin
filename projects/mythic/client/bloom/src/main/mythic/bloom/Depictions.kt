package mythic.bloom

import mythic.drawing.Canvas
import mythic.glowing.cropStack
import mythic.spatial.Vector4
import mythic.typography.TextStyle
import org.joml.Vector4i

data class DeferredTextStyle(
    val font: Int,
    val size: Float,
    val color: Vector4
)

typealias TextStyleSource = (Canvas) -> TextStyle

fun resolve(style: DeferredTextStyle): TextStyleSource = { c: Canvas ->
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

fun clip(bounds: Bounds, depiction: Depiction): Depiction = { b, c ->
  //  if (isInside(outer, inner))
//    depiction
//  else if (isOutside(outer, inner))
//  { b: Bounds, c: Canvas -> val k = 0 }
//  else {
  val viewport = c.flipViewport(bounds.toVector4i())
  cropStack(viewport) {
    depiction(b, c)
  }
//  }
}