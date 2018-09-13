package generation

import mythic.spatial.Vector2
import mythic.spatial.minMax
import randomly.Dice

typealias Grid<T> = (x: Float, y: Float) -> T

data class VoronoiAnchor<T>(
    val position: Vector2,
    val value: T
)

fun <T> voronoiAnchors(values: List<T>, count: Int, dice: Dice): List<VoronoiAnchor<T>> =
    (1..count)
        .map {
          VoronoiAnchor(
              position = Vector2(
                  dice.getFloat(0f, 1f),
                  dice.getFloat(0f, 1f)
              ),
              value = dice.getItem(values)
          )
        }

private fun <T> logVoronoiGrid(width: Int, height: Int, list: List<T>) {
  for (y in 0 until height) {
    val line = (0 until width).map { x -> list[y * width + x].toString().substring(0, 1) }
        .joinToString(" ")
    println(line)
  }
}

fun <T> voronoi(width: Int, height: Int, anchors: List<VoronoiAnchor<T>>): Grid<T> {
  val total = width * height
  val list: List<T> = (0 until total).map { i ->
    val y = i / width
    val x = i - y * width
    val position = Vector2(x.toFloat() / width, y.toFloat() / height)
    anchors.sortedBy { position.distance(it.position) }
        .first().value
  }

// The returned grid expects x and y to be between 0..1f

  return { x, y ->
    val x2 = x * width
    val y2 = y * height
    val index = Math.min((y2 * width + x2).toInt(), list.size - 1)
    list[index]
  }
}

fun <T> clampGrid(grid: Grid<T>): Grid<T> = { x, y ->
  grid(
      minMax(x, 0f, 1f),
      minMax(y, 0f, 1f)
  )
}