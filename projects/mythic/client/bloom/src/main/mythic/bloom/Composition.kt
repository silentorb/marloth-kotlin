package mythic.bloom

import org.joml.minus

fun <A, B> wrap(outer: (A) -> A, inner: (B) -> A): (B) -> A =
    { outer(inner(it)) }

fun join(first: FlowerOld, second: FlowerOld): FlowerOld =
    { first(it) + second(it) }

fun accumulatedBounds(boxes: FlatBoxes): Bounds {
  assert(boxes.any())
  val start = boxes.first().bounds.position
  val end = boxes.sortedByDescending { it.bounds.end.y }.first().bounds.end
  return Bounds(start, end - start)
}

//fun joinChildren(child: Flower, parent: (SeedOld) -> Flower): Flower =
//    {
//      val childBoxes = child(it)
//      val start = childBoxes.first().bounds.position
//      val end = childBoxes.sortedByDescending { it.bounds.end.y }.first().bounds.end
//      val childBounds = Bounds(start, end - start)
//
//      parent(SeedOld(it.bag, childBounds))(it)
//          .plus(childBoxes)
//    }
