package mythic.bloom

import mythic.bloom.next.Box
import mythic.bloom.next.Flower
import org.joml.Vector2i
import org.joml.minus
import org.joml.plus

typealias FixedChildArranger = (Bounds) -> List<Bounds>
typealias ParentFlower = (List<Flower>) -> Flower
typealias ItemFlower<T> = (T) -> FlowerOld

data class ListItem<T>(
    val length: Int,
    val itemFlower: ItemFlower<T>
)

typealias ListFlower<T> = (Collection<T>) -> FlowerOld

typealias PreparedChildren<T> = Triple<Collection<T>, List<FlowerOld>, List<Bounds>>

typealias ChildArranger<T> = (Bounds, Collection<T>) -> Triple<Collection<T>, List<FlowerOld>, List<Bounds>>

fun <T> children(arrangement: LengthArrangement, listItem: ListItem<T>): ChildArranger<T> = { bounds, items ->
  val lengths = items.map { listItem.length }
  val flowers = items.map(listItem.itemFlower)
  Triple(items, flowers, arrangement(bounds, lengths))
}

fun applyBounds(bag: StateBag, children: List<FlowerOld>, bounds: List<Bounds>): FlatBoxes =
    bounds.zip(children) { b, child -> child(SeedOld(bag, b)).boxes }
        .flatten()

fun applyBounds(arranger: FixedChildArranger): (List<FlowerOld>) -> FlowerOld = { flowers ->
  {
    Blossom(
        boxes = applyBounds(it.bag, flowers, arranger(it.bounds)),
        bounds = emptyBounds
    )
  }
}

fun listOld(plane: Plane, padding: Int): (List<FlowerOld>) -> FlowerOld = { children ->
  { seed ->
    val normalize = normalizeBounds(plane)
    var lastBounds = seed.bounds
    Blossom(
        boxes = children.flatMap { flower ->
          val blossom = flower(seed.copy(bounds = lastBounds))
          val farthest = blossom.boxes.map { normalize(it.bounds).right }.sortedDescending().first()
          val normalizedLastBounds = normalize(lastBounds)

          val offset = Vector2i(farthest - lastBounds.top + padding, 0)
          lastBounds = normalize(Bounds(
              position = normalizedLastBounds.position + offset,
              dimensions = normalizedLastBounds.dimensions - offset
          ))
          blossom.boxes
        },
        bounds = emptyBounds
    )
  }
}

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

fun fixedList(plane: Plane, padding: Int, lengths: List<Int?>): ParentFlower =
    applyBounds(fixedLengthArranger(plane, padding, lengths))
