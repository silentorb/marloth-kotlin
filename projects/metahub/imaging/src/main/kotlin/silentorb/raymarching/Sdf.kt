package silentorb.raymarching

import mythic.ent.firstSortedBy
import mythic.spatial.Vector3

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
    val gap = Math.abs(sorted[0].value - sorted[1].value)
    if (sorted[0].value < range && sorted[1].value < range && gap <= range)
      PointDistance(0f)
    else
      sorted.first()
  } else
    sorted.first()
}

fun diff(a: Sdf, b: Sdf): Sdf = { point ->
  PointDistance(Math.max(a(point).value, b(point).value))
}
