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

fun applyBounds(bag: StateBag, children: List<Flower>, bounds: List<Bounds>): Boxes =
    bounds.zip(children) { b, child -> child(Seed(bag, b)) }
        .flatten()

fun applyBounds(arranger: FixedChildArranger): ParentFlower = { children ->
  { applyBounds(it.bag, children, arranger(it.bounds)) }
}

fun depict(depiction: StateDepiction): Flower = { s ->
  listOf(
      Box(
          bounds = s.bounds,
          depiction = depiction(s)
      )
  )
}

fun depict(depiction: Depiction): Flower =
    depict { s: Seed -> depiction }

typealias StateDepiction = (Seed) -> Depiction

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

fun <T> list(arranger: ChildArranger<T>): ListFlower<T> = { items ->
  { seed ->
    val (_, flowers, itemBounds) = arranger(seed.bounds, items)
    applyBounds(seed.bag, flowers, itemBounds)
  }
}

fun <T> getExistingOrNewState(initializer: () -> T): (Any?) -> T = { state ->
  if (state != null)
    state as T
  else
    initializer()
}
