package mythic.bloom

import mythic.glowing.cropStack

fun label(style: TextStyleSource, content: String): Depiction = { b, c ->
  val position = b.position
  c.drawText(position, style(c), content)
}

fun clipBox(bounds: Bounds, depiction: Depiction): Depiction = { b, c ->
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