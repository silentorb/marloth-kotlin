package generation.elements

import mythic.spatial.Vector3i

val sideDirections: Map<Int, Vector3i> = mapOf(
    0 to Vector3i(0, 0, 1),
    1 to Vector3i(0, 0, -1),
    2 to Vector3i(1, 0, 0),
    3 to Vector3i(0, 1, 0),
    4 to Vector3i(-1, 0, 0),
    5 to Vector3i(0, -1, 0)
)

fun getRotatedPolyominoes(polyominoes: Set<Polyomino>): Map<Polyomino, List<Polyomino>> {
  val nonFree = polyominoes.filter { it != rotatePolyomino(it, 1) }
  return nonFree.associateWith { polyomino ->
    (1..3)
        .map { turns -> rotatePolyomino(polyomino, turns) }
  }
}

fun addRotatedPolyominoes(polyominoes: Set<Polyomino>, rotatedPolyominoes: Map<Polyomino, Set<Polyomino>>) =
    polyominoes
        .plus(rotatedPolyominoes.flatMap { it.value })
