package generation.architecture.misc

import mythic.spatial.Vector3
import mythic.spatial.nearestFast
import randomly.Dice

// Within 0..1
typealias FloatUnit = Float

typealias Grid<T> = (Vector3) -> T

data class VoronoiAnchor<T>(
    val position: Vector3,
    val value: T
)

fun <T>voronoiAnchors(values: List<T>, count: Int, dice: Dice, start: Vector3, end: Vector3): List<VoronoiAnchor<T>> =
    (1..count)
        .map {
          VoronoiAnchor(
              position = Vector3(
                  dice.getFloat(start.x, end.x),
                  dice.getFloat(start.y, end.y),
                  dice.getFloat(start.z, end.z)
              ),
              value = dice.takeOne(values)
          )
        }

//private fun <T> logVoronoiGrid(width: Int, height: Int, list: List<T>) {
//  for (y in 0 until height) {
//    val line = (0 until width).map { x -> list[y * width + x].toString().substring(0, 1) }
//        .joinToString(" ")
//    println(line)
//  }
//}

fun <T>voronoi(anchors: List<VoronoiAnchor<T>>): Grid<T> {
//  val total = width * depth * height
//  val list: List<Int> = (0 until total).map { i ->
//    val y = i / width
//    val x = i - y * width
//    val z = i
//    val position = Vector3(x.toFloat() / width, y.toFloat() / depth, z.toFloat() / height)
//    anchors.firstSortedBy { position.distance(it.position) }
//        .value
//  }

// The returned grid expects x and y to be between 0..1f

  val list = anchors.map { Pair(it.position, it.value) }
  return nearestFast(list)
//  return { point ->
//    val x2 = x * width
//    val y2 = y * depth
//    val index = Math.min((y2 * width + x2).toInt(), list.size - 1)
//    list[index]
//  }
}

//fun <T> cachedVoronoi(width: Int, height: Int, anchors: List<VoronoiAnchor<T>>): Grid<T> {
//  val cellResolution = anchors.size / 10
//
//}

//fun clampGrid(grid: Grid): Grid = { (x, y, z) ->
//  grid(
//      minMax(x, 0f, 1f),
//      minMax(y, 0f, 1f),
//      minMax(z, 0f, 1f)
//  )
//}
