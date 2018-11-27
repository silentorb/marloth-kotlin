package mythic.bloom

typealias FixedChildArranger = (Bounds) -> List<Bounds>
typealias ParentFlower = (Bounds, List<Flower>) -> Boxes
typealias Flower = (Bounds) -> Boxes

fun horizontal(padding: Int, lengths: List<Int?>): FixedChildArranger = { bounds ->
  horizontal(padding)(bounds, resolveLengths(bounds.dimensions.x, lengths))
}

fun vertical(padding: Int, lengths: List<Int?>): FixedChildArranger = { bounds ->
  vertical(padding)(bounds, resolveLengths(bounds.dimensions.y, lengths))
}

fun arrangeChildren(bounds: List<Bounds>, children: List<Flower>): Boxes =
    bounds.zip(children) { b, child -> child(b) }
        .flatten()

fun arrangeChildren(bounds: Bounds, children: List<Flower>, arranger: FixedChildArranger): Boxes =
    arrangeChildren(arranger(bounds), children)

fun arrangeChildren(arranger: FixedChildArranger): ParentFlower = { bounds, children ->
  arrangeChildren(bounds, children, arranger)
}

fun depict(depiction: Depiction): Flower = { b ->
  listOf(
      Box(b, depiction = depiction)
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
    arrangeChildren(arrangement(b, lengths), flowers)
  }
}