package generation.elements

import mythic.spatial.Vector3i
import kotlin.reflect.full.memberProperties

typealias Polyomino = Map<Vector3i, Block>

fun rotatePosition(position: Vector3i, turns: Int): Vector3i =
    when(turns) {
      1 -> position.copy(x = -position.y, y = position.x)
      2 -> position.copy(x = -position.x, y = -position.y)
      3 -> position.copy(x = position.y, y = -position.x)
      else -> throw Error("Not supported")
    }

fun rotatePolyomino(polyomino: Polyomino, turns: Int): Polyomino {
  assert(turns in 0..3)

  if (turns == 0)
    return polyomino

  val (centered, notCentered) = polyomino.entries
      .partition { it.key.x == 0 && it.key.y == 0 }

  return centered
      .associate { it.toPair() }
      .plus(notCentered.map { (position, block) ->
        Pair(rotatePosition(position, turns), rotateBlock(block, turns))
      })
}

// Try larger polyominoes first
fun preparePolyominoes(polyominoes: Set<Polyomino>): List<Polyomino> =
    polyominoes.toList().sortedByDescending { it.size }

fun enumeratePolyominoes(polyominoes: Any): List<Polyomino> {
  return polyominoes.javaClass.kotlin.memberProperties.map { member ->
    @Suppress("UNCHECKED_CAST")
    member.get(polyominoes) as Polyomino
  }
}
