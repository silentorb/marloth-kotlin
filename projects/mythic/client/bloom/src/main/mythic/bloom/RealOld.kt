package mythic.bloom

import org.joml.Vector2i

data class ClickBox<T>(
    val bounds: Bounds,
    val value: T
)

fun <T> filterMouseOverBoxes(boxes: List<ClickBox<T>>, mousePosition: Vector2i): ClickBox<T>? {
  return boxes.filter { box -> isInBounds(mousePosition, box.bounds) }.firstOrNull()
}
