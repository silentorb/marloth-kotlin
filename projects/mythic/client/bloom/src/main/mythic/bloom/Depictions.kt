package mythic.bloom

import mythic.drawing.globalFonts
import mythic.glowing.cropStack
import mythic.spatial.Vector2
import mythic.spatial.toVector2i
import mythic.typography.TextConfiguration
import mythic.typography.calculateTextDimensions

fun textDepiction(style: IndexedTextStyle, content: String): Depiction = { b, c ->
  val position = b.position
  c.drawText(position, resolve(style)(c.fonts), content)
}

fun label(style: IndexedTextStyle, content: String): Flower = { seed ->
  val config = TextConfiguration(content, Vector2(), resolve(style)(globalFonts()))
  val dimensions = calculateTextDimensions(config)
  listOf(
      Box(
          bounds = seed.bounds.copy(
              dimensions = dimensions.toVector2i()
          ),
          depiction = textDepiction(style, content)
      )
  )
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