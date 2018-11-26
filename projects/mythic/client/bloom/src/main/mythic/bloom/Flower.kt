package mythic.bloom

typealias FixedChildArranger = (Bounds) -> List<Bounds>
typealias ParentFlower = (Bounds, List<Flower>) -> Boxes
typealias Flower = (Bounds) -> Boxes

fun horizontal(padding: Int, lengths: List<Int?>): FixedChildArranger = { bounds ->
  arrangeHorizontal(padding)(bounds, resolveLengths(bounds.dimensions.x, lengths))
}

fun arrangeChildren(arranger: FixedChildArranger, bounds: Bounds, children: List<Flower>): Boxes =
    arranger(bounds).zip(children) { b, child -> child(b) }
        .flatten()

fun arrangeChildren(arranger: FixedChildArranger): ParentFlower = { bounds, children ->
  arrangeChildren(arranger, bounds, children)
}

fun depict(depiction: Depiction): Flower = { b ->
  listOf(
      Box(b, depiction = depiction)
  )
}