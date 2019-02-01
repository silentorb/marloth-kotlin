package silentorb.raymarching

import mythic.ent.firstSortedBy
import mythic.spatial.Vector3
import mythic.spatial.cubicIn
import mythic.spatial.quadIn
import mythic.spatial.quadInOut

typealias Sdf = (Vector3) -> PointDistance

fun sphereSdf(center: Vector3, radius: Float): Sdf = { point ->
  PointDistance(point.distance(center) - radius)
}

fun plus(vararg items: Sdf): Sdf = { point ->
  items.map { it(point) }
      .firstSortedBy { it.value }
}

fun blend(range: Float, vararg items: Sdf): Sdf = { point ->
  val sorted = items.map { it(point) }
      .sortedBy { it.value }

  if (sorted.size > 1) {
    val a = sorted[0].value
    val b = sorted[1].value
    val gap = Math.abs(a - b)
    val scale = Math.min(range, gap)
    val curved = quadIn(scale)
//    val rangeValue = (range + Math.min(range, gap)) / 2f
    val scaledRange = range + curved
    val j = a + b - scaledRange
    val c = listOf(a, j).sorted().first()
    if (point.x == 0f && point.z == 0f) {
//      if (point.x > -0.01f && point.x < 0.01f && point.z > -0.01f && point.z < 0.01f) {
      println("${point.z} $a, $j, $gap")
    }
    if (isRayHit(j)) {
      val k = 0
    }
    PointDistance(c)
  } else
    sorted.first()
}

fun diff(a: Sdf, b: Sdf): Sdf = { point ->
  PointDistance(Math.max(a(point).value, b(point).value))
}
