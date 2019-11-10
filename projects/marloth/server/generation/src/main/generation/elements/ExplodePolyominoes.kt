package generation.elements

import mythic.spatial.Vector3i

data class MappedPolyomino(
    val original: Polyomino,
    val offset: Vector3i = Vector3i.zero,
    val turns: Int = 0
)

typealias PolyominoMap = Map<Polyomino, MappedPolyomino>

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
          Pair(translatePolyomino(-offset)(polyomino), info.copy(offset = offset))
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
