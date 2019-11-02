package generation.elements

typealias Side = Set<Any>
typealias Block = List<Side>

typealias TypedSide<T> = Set<T>
typealias TypedBlock<T> = List<TypedSide<T>>

fun newBlock(top: Side, bottom: Side, east: Side, north: Side, west: Side, south: Side) =
    listOf(
        top,
        bottom,
        east,
        north,
        west,
        south
    )
fun rotateBlock(sides: Block, turns: Int): Block {
  val horizontalSides = sides
      .drop(2)

  val rotatedSides = horizontalSides
      .drop(turns)
      .plus(horizontalSides.take(turns))

  return sides
      .take(2)
      .plus(rotatedSides)
}
