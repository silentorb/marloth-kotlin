package mythic.bloom

import org.joml.Vector2i

data class ClickBox<T>(
    val bounds: Bounds,
    val value: T
)

data class PartialBox(
    val length: Int,
    val depiction: Depiction? = null,
    val handler: Any? = null
)

fun listContentLength(padding: Int, lengths: Collection<Int>): Int =
    lengths.sum() + (lengths.size + 1) * padding

fun arrangeListComplex(arrangement: LengthArrangement, panels: List<PartialBox>, bounds: Bounds): List<FlatBox> {
  return arrangement(bounds, panels.map { it.length })
      .zip(panels, { a, b -> FlatBox(a, b.depiction) })
}

fun <T> filterMouseOverBoxes(boxes: List<ClickBox<T>>, mousePosition: Vector2i): ClickBox<T>? {
  return boxes.filter { box -> isInBounds(mousePosition, box.bounds) }.firstOrNull()
}
