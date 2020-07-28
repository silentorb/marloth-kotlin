package generation.general

fun rotateSides(turns: Int): (SideMap) -> SideMap = { sides ->
  val horizontal = horizontalDirectionList.map { sides[it]!! }
  val normalizedTurns = turns % 4

  val spunSides = horizontal
      .takeLast(normalizedTurns)
      .plus(horizontal.take(4 - normalizedTurns))

  val horizontalRotatedSides = horizontalDirectionList.zip(spunSides) { a, b -> Pair(a, b) }.associate { it }

  val verticalRotatedSides = sides
      .filterKeys { verticalSides.contains(it) }

  verticalRotatedSides
      .plus(horizontalRotatedSides)
}
