package generation.elements

import mythic.spatial.Vector3i
import kotlin.reflect.full.memberProperties

typealias Polyomino = Map<Vector3i, Block>

fun rotatePosition(position: Vector3i, turns: Int): Vector3i =
    when (turns) {
      1 -> position.copy(x = -position.y, y = position.x)
      2 -> position.copy(x = -position.x, y = -position.y)
      3 -> position.copy(x = position.y, y = -position.x)
      else -> throw Error("Not supported")
    }

fun translatePolyomino(offset: Vector3i): (Polyomino) -> Polyomino = { polyomino ->
  polyomino.mapKeys { it.key + offset }
}

fun enumeratePolyominoes(polyominoes: Any): List<Polyomino> {
  return polyominoes.javaClass.kotlin.memberProperties.map { member ->
    @Suppress("UNCHECKED_CAST")
    member.get(polyominoes) as Polyomino
  }
}

fun logPolyomino(polyomino: Polyomino) {
  println(polyomino.hashCode())
  for (block in polyomino) {
    println("  ${block.key}")
    for (side in block.value.sides) {
      println("    ${side.key} ${side.value}")
    }
  }
}
