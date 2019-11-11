package generation.elements

import mythic.spatial.Vector3i

data class MappedPolyomino(
    val original: Polyomino,
    val offset: Vector3i = Vector3i.zero,
    val turns: Int = 0
)

typealias PolyominoMap = Map<Polyomino, MappedPolyomino>

fun rotateSides(turns: Int): (Sides) -> Sides = { sides ->
  val horizontal = horizontalSideList.map { sides[it] ?: setOf() }
  val normalizedTurns = turns % 4

  val spunSides = horizontal
      .takeLast(normalizedTurns)
      .plus(horizontal.take(4 - normalizedTurns))

  val rotatedSides = horizontalSideList.zip(spunSides) { a, b -> Pair(a, b) }.associate { it }

  sides
      .filterKeys { verticalSides.contains(it) }
      .plus(rotatedSides)
}

fun rotatePolyomino(polyomino: Polyomino, turns: Int): Polyomino {
  assert(turns in 0..3)

  if (turns == 0)
    return polyomino

//  val (centered, notCentered) = polyomino.entries
//      .partition { it.key.x == 0 && it.key.y == 0 }

  return polyomino
//      .associate { it.toPair() }
      .entries.associate { (position, block) ->
    Pair(rotatePosition(position, turns), block.copy(sides = rotateSides(turns)(block.sides)))
  }
}

fun getRotatedPolyominoes(polyominoes: Set<Polyomino>): PolyominoMap {
  val nonFree = polyominoes.filter {
    val k = rotatePolyomino(it, 1)
    it != rotatePolyomino(it, 1) }
  return nonFree.flatMap { polyomino ->
    (1..3)
        .map { turns ->
          Pair(rotatePolyomino(polyomino, turns), MappedPolyomino(original = polyomino, turns = turns))
        }
  }
      .associate { it }
}

fun getTranslatedPolyominoes(polyominoes: Set<Polyomino>): PolyominoMap {
  return polyominoes.flatMap { polyomino ->
    polyomino
        .filter { it.key != Vector3i.zero }
        .map { (blockPosition, _) ->
          val offset = -blockPosition
          Pair(translatePolyomino(offset)(polyomino), MappedPolyomino(original = polyomino, offset = offset))
        }
  }
      .associate { it }
}

fun getTranslatedPolyominoes2(polyominoes: PolyominoMap): PolyominoMap {
  return polyominoes.flatMap { (polyomino, info) ->
    polyomino
        .filter { it.key != Vector3i.zero }
        .map { (blockPosition, _) ->
          val offset = -blockPosition
          Pair(translatePolyomino(offset)(polyomino), info.copy(offset = offset))
        }
  }
      .associate { it }
}

fun explodePolyominoes(polyominoes: Set<Polyomino>): PolyominoMap {
  val rotatedPolyominoes = getRotatedPolyominoes(polyominoes)
  val translatedPolyominoes = getTranslatedPolyominoes(polyominoes)
  val translatedRotatedPolyominoes = getTranslatedPolyominoes2(rotatedPolyominoes)
  val variations = rotatedPolyominoes.plus(translatedPolyominoes).plus(translatedRotatedPolyominoes)
  val originals = polyominoes
      .filter { !variations.containsKey(it) }
      .associateWith { MappedPolyomino(original = it) }
  return variations.plus(originals)
}
