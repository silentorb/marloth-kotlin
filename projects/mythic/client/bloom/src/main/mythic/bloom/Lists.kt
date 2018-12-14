package mythic.bloom

import org.joml.Vector2i
import org.joml.minus
import org.joml.plus

typealias FixedChildArranger = (Bounds) -> List<Bounds>
typealias ParentFlower = (List<Flower>) -> Flower
typealias ItemFlower<T> = (T) -> Flower

data class ListItem<T>(
    val length: Int,
    val itemFlower: ItemFlower<T>
)

typealias ListFlower<T> = (Collection<T>) -> Flower

typealias PreparedChildren<T> = Triple<Collection<T>, List<Flower>, List<Bounds>>

typealias ChildArranger<T> = (Bounds, Collection<T>) -> Triple<Collection<T>, List<Flower>, List<Bounds>>

fun <T> children(arrangement: LengthArrangement, listItem: ListItem<T>): ChildArranger<T> = { bounds, items ->
  val lengths = items.map { listItem.length }
  val flowers = items.map(listItem.itemFlower)
  Triple(items, flowers, arrangement(bounds, lengths))
}

fun applyBounds(bag: StateBag, children: List<Flower>, bounds: List<Bounds>): Boxes =
    bounds.zip(children) { b, child -> child(Seed(bag, b)) }
        .flatten()

fun applyBounds(arranger: FixedChildArranger): ParentFlower = { children ->
  { applyBounds(it.bag, children, arranger(it.bounds)) }
}

fun list(plane: Plane, padding: Int): (List<Flower>) -> Flower = { children ->
  { seed ->
    val normalize = normalizeBounds(plane)
    var lastBounds = seed.bounds
    children.flatMap { flower ->
      val boxes = flower(seed.copy(bounds = lastBounds))
      val farthest = boxes.map { normalize(it.bounds).right }.sortedDescending().first()
      val normalizedLastBounds = normalize(lastBounds)

      val offset = Vector2i(farthest - lastBounds.top + padding, 0)
      lastBounds = normalize(Bounds(
          position = normalizedLastBounds.position + offset,
          dimensions = normalizedLastBounds.dimensions - offset
      ))
      boxes
//          .map {
//            it.copy(
//                bounds = it.bounds.copy(
//                    position = it.bounds.position + plane(offset)
//                )
//            )
//          }
    }
  }
}

fun <T> list(arranger: ChildArranger<T>): ListFlower<T> = { items ->
  { seed ->
    val (_, flowers, itemBounds) = arranger(seed.bounds, items)
    applyBounds(seed.bag, flowers, itemBounds)
  }
}

fun <T> list(arrangement: LengthArrangement, listItem: ListItem<T>): ListFlower<T> = { items ->
  { b ->
    val lengths = items.map { listItem.length }
    val flowers = items.map(listItem.itemFlower)
    applyBounds(b.bag, flowers, arrangement(b.bounds, lengths))
  }
}

fun fixedList(plane: Plane, padding: Int, lengths: List<Int?>): ParentFlower =
    applyBounds(fixedLengthArranger(plane, padding, lengths))
//
//fun <T> list(arrangement: LengthArrangement): (List<Flower>) -> Flower = { flowers ->
//  applyBounds(arrangement)(flowers)
//}
