package mythic.bloom

import org.joml.minus

fun <A, B> wrap(outer: (A) -> A, inner: (B) -> A): (B) -> A =
    { outer(inner(it)) }

fun join(first: Flower, second: Flower): Flower =
    { first(it).plus(second(it)) }

fun joinChildren(child: Flower, parent: (Seed) -> Flower): Flower =
    {
      val childBoxes = child(it)
      val start = childBoxes.first().bounds.position
      val end = childBoxes.last().bounds.end
      val childBounds = Bounds(start, end - start)

      parent(Seed(it.bag, childBounds))(it)
          .plus(childBoxes)
    }
