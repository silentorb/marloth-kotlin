package generation.elements

typealias Block = Map<Direction, Side>

typealias TypedSide<T> = Set<T>
typealias TypedBlock<T> = List<TypedSide<T>>

fun newBlock(top: Side, bottom: Side, east: Side, north: Side, west: Side, south: Side) =
    mapOf(
        Direction.up to top,
        Direction.down to bottom,
        Direction.east to east,
        Direction.north to north,
        Direction.west to west,
        Direction.south to south
    )

fun rotateBlock(sides: Block, turns: Int): Block {
  val horizontal = horizontalSideList.map { sides[it] ?: setOf() }
  val normalizedTurns = turns % 4

  val spunSides = horizontal
      .takeLast(normalizedTurns)
      .plus(horizontal.take(4 - normalizedTurns))

  val rotatedSides = horizontalSideList.zip(spunSides) {a, b -> Pair(a, b)}.associate { it }

  return sides
      .filterKeys { verticalSides.contains(it) }
      .plus(rotatedSides)
}
