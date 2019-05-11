package mythic.bloom

import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import mythic.bloom.next.Seed
import org.joml.Vector2i
import org.joml.minus
import org.joml.plus

typealias FixedChildArranger = (Vector2i) -> List<Bounds>
typealias ParentFlower = (List<Flower>) -> Flower

typealias PreparedChildren<T> = Triple<Collection<T>, List<Flower>, List<Bounds>>

fun applyBounds(bag: StateBag, children: List<Flower>, bounds: List<Bounds>): List<Box> =
    bounds.zip(children) { b, child -> child(Seed(bag, b.dimensions)).boxes }
        .flatten()

fun applyBounds(arranger: FixedChildArranger): (List<Flower>) -> Flower = { flowers ->
  { seed ->
    Box(
        boxes = applyBounds(seed.bag, flowers, arranger(seed.dimensions)),
        bounds = emptyBounds
    )
  }
}

//fun listOld(plane: Plane, padding: Int): (List<FlowerOld>) -> FlowerOld = { children ->
//  { seed ->
//    val normalize = normalizeBounds(plane)
//    var lastBounds = seed.bounds
//    Blossom(
//        boxes = children.flatMap { flower ->
//          val blossom = flower(seed.copy(bounds = lastBounds))
//          val farthest = blossom.boxes.map { normalize(it.bounds).right }.sortedDescending().first()
//          val normalizedLastBounds = normalize(lastBounds)
//
//          val offset = Vector2i(farthest - lastBounds.top + padding, 0)
//          lastBounds = normalize(Bounds(
//              position = normalizedLastBounds.position + offset,
//              dimensions = normalizedLastBounds.dimensions - offset
//          ))
//          blossom.boxes
//        },
//        bounds = emptyBounds
//    )
//  }
//}

fun list(plane: Plane, spacing: Int): (List<Flower>) -> Flower = { children ->
  { seed ->
    var lastOffset = 0
    var otherLength = 0
    val boxes = children.mapIndexed { i, flower ->
      val initialBox = flower(seed)
      val box = initialBox.copy(
          bounds = initialBox.bounds.copy(
              position = initialBox.bounds.position + plane(Vector2i(lastOffset, 0))
          )
      )
      val childDimensions = plane(box.bounds.end)
      if (childDimensions.y > otherLength)
        otherLength = childDimensions.y

      lastOffset = childDimensions.x + if (i != children.size - 1) spacing else 0

      box
    }

    Box(
        name = "list",
        boxes = boxes,
        bounds = Bounds(
            dimensions = plane(Vector2i(lastOffset, otherLength))
        )
    )
  }
}

//fun fixedListOld(plane: Plane, padding: Int, lengths: List<Int?>): ParentFlower =
//    applyBounds(fixedLengthArranger(plane, padding, lengths))

fun fixedList(plane: Plane, padding: Int, lengths: List<Int?>): ParentFlower = { flowers ->
  { seed ->
    val boundsList = fixedLengthArranger(plane, padding, lengths)(seed.dimensions)
    Box(
        name = "fixedList",
        bounds = Bounds(dimensions = seed.dimensions),
        boxes = flowers.zip(boundsList) { flower, bounds ->
          val box = flower(seed.copy(dimensions = bounds.dimensions))
          box.copy(
              bounds = box.bounds.copy(
                  position = bounds.position + box.bounds.position
              )
          )
        }
    )
  }
}
