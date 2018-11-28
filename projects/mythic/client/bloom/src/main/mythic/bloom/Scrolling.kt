package mythic.bloom

import mythic.drawing.Brush
import mythic.spatial.Vector4
import mythic.spatial.toVector4
import mythic.spatial.xy
import mythic.spatial.zw
import org.joml.Vector2i
import org.joml.plus

fun scrollbar(offset: Int, contentLength: Int): Depiction = { b, c ->
  val width = 15
  val bounds = Bounds(
      x = b.end.x - width - 2,
      y = offset * b.dimensions.y / contentLength,
      width = width,
      height = b.dimensions.y * b.dimensions.y / contentLength
  )

  val viewport = bounds.toVector4i().toVector4()

  c.drawSquare(viewport.xy(), viewport.zw, c.solid(Vector4(0.6f, 0.6f, 0.6f, 1f)))
}

fun clipChildren(child: Flower): Flower = { b ->
  child(b).map { box ->
    val depiction = if (box.depiction != null)
      clip(b, box.depiction)
    else
      null

    box.copy(
        depiction = depiction
    )
  }
}

fun offset(value: Vector2i, flower: Flower): Flower =
    { flower(Bounds(it.position + value, it.dimensions)) }

val scrolling: (Flower) -> Flower = { child ->
  joinChildren(
      clipChildren(offset(Vector2i(0, -100), child)),
      { depict(scrollbar(100, it.dimensions.y)) }
  )
}