package mythic.bloom

import org.joml.minus

fun <A, B> wrap(outer: (A) -> A, inner: (B) -> A): (B) -> A =
    { outer(inner(it)) }

fun join(first: Flower, second: Flower): Flower =
    { first(it).plus(second(it)) }

fun accumulatedBounds(boxes: Boxes): Bounds {
  assert(boxes.any())
  val start = boxes.first().bounds.position
  val end = boxes.sortedByDescending { it.bounds.end.y }.first().bounds.end
  return Bounds(start, end - start)
}

//fun joinChildren(child: Flower, parent: (Seed) -> Flower): Flower =
//    {
//      val childBoxes = child(it)
//      val start = childBoxes.first().bounds.position
//      val end = childBoxes.sortedByDescending { it.bounds.end.y }.first().bounds.end
//      val childBounds = Bounds(start, end - start)
//
//      parent(Seed(it.bag, childBounds))(it)
//          .plus(childBoxes)
//    }
