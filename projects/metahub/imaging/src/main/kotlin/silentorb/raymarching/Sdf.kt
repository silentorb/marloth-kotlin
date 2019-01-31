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

fun diff(a: Sdf, b: Sdf): Sdf = { point ->
  PointDistance(Math.max(a(point).value, b(point).value))
}
