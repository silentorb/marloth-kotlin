package generation.general

import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.nearestFast
import silentorb.mythic.randomly.Dice

// Within 0..1
typealias FloatUnit = Float

typealias Grid<T> = (Vector3) -> T

data class VoronoiAnchor<T>(
    val position: Vector3,
    val value: T
)

typealias VoronoiAnchors<T> = List<VoronoiAnchor<T>>

fun <T>voronoiAnchors(values: List<T>, count: Int, dice: Dice, start: Vector3, end: Vector3): VoronoiAnchors<T> =
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
