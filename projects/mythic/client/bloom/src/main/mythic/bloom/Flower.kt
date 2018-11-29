package mythic.bloom

data class Seed(
    val bag: StateBag,
    val bounds: Bounds
)

typealias FixedChildArranger = (Bounds) -> List<Bounds>
typealias ParentFlower = (List<Flower>) -> Flower
typealias Flower = (Seed) -> Boxes

fun horizontal(padding: Int, lengths: List<Int?>): FixedChildArranger = { bounds ->
  horizontal(padding)(bounds, resolveLengths(bounds.dimensions.x, lengths))
}

fun vertical(padding: Int, lengths: List<Int?>): FixedChildArranger = { bounds ->
  vertical(padding)(bounds, resolveLengths(bounds.dimensions.y, lengths))
}

fun arrangeChildren(bag: StateBag, bounds: List<Bounds>, children: List<Flower>): Boxes =
    bounds.zip(children) { b, child -> child(Seed(bag, b)) }
        .flatten()

fun arrangeChildren(bag: StateBag, bounds: Bounds, children: List<Flower>, arranger: FixedChildArranger): Boxes =
    arrangeChildren(bag, arranger(bounds), children)

fun arrangeChildren(arranger: FixedChildArranger): ParentFlower = { children ->
  { arrangeChildren(it.bag, it.bounds, children, arranger) }
}

fun depict(depiction: Depiction): Flower = { b ->
  listOf(
      Box(
          bounds = b.bounds,
          depiction = depiction
      )
  )
}

typealias ItemFlower<T> = (T) -> Flower

data class ListItem<T>(
    val length: Int,
    val flower: ItemFlower<T>
)

typealias ListFlower<T> = (Collection<T>) -> Flower

fun <T> list(arrangement: LengthArrangement, listItem: ListItem<T>): ListFlower<T> = { items ->
  { b ->
    val lengths = items.map { listItem.length }
    val flowers = items.map(listItem.flower)
    arrangeChildren(b.bag, arrangement(b.bounds, lengths), flowers)
  }
}