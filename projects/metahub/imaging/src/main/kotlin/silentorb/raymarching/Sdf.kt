package silentorb.raymarching

import silentorb.mythic.spatial.*

typealias SdfHook = () -> Unit

data class SdfHitInfo(
    var geometry: Geometry? = null
)

typealias Sdf = (SdfHook, SdfHitInfo, Vector3) -> Float
typealias RaySdf = (Ray) -> Sdf

typealias MinimalSdf = (Vector3) -> Float

data class BoundedSdf(
    val sdf: Sdf,
    val position: Vector3,
    val radius: Float
)

fun fromMinimal(geometry: Geometry, sdf: MinimalSdf): Sdf = { hook, info, point ->
  hook()
  info.geometry = geometry
  sdf(point)
}

fun sphereNormal(center: Vector3): Normal = { _, point -> (point - center).normalize() }

fun sphereSdf(center: Vector3, radius: Float): Sdf = fromMinimal(Geometry(normal = sphereNormal(center))) { point ->
  point.distance(center) - radius
}

fun boundedSphere(center: Vector3, radius: Float) =
    BoundedSdf(
        sdf = sphereSdf(center, radius),
        position = center,
        radius = radius
    )

fun diff(a: Float, b: Float): Float =
    Math.max(a, -b)

//fun diff(a: Sdf, b: Sdf): Sdf = { hook, info, point ->
//  PointDistance(diff(a(hook, point).value, b(hook, point).value))
//}

fun plusBounded(items: List<BoundedSdf>): RaySdf = { ray ->
  val b = Vector3(ray.position) + Vector3(ray.direction)
  val project = projectPointOnLine(Vector3(ray.position), b)
  val filtered = items
      .filter {
        val projected = project(it.position)
        withinRangeFast(projected, it.position, it.radius)
      }
      .map { it.sdf }
//  val filtered = items.take(1).map { it.sdf }
//  val filtered = listOf(items[0].sdf)
  plusSdf(filtered)
}

fun plusSdf(items: List<Sdf>): Sdf = { hook, info, point ->
  var nearest: Float = rayMissValue
  for (item in items) {
    val subInfo = SdfHitInfo()
    val distance = item(hook, subInfo, point)
    if (distance < nearest) {
      nearest = distance
      info.geometry = subInfo.geometry
    }
    break
  }

  nearest
}

//fun plusDetailed(items: List<Sdf>, hook: SdfHook, point: Vector3): Pair<Sdf?, PointDistance> {
//  var sdf: Sdf? = null
//  var nearest: PointDistance = rayMiss
//  for (item in items) {
//    val distance = item(hook, point)
//    if (distance.value < nearest.value) {
//      sdf = item
//      nearest = distance
//    }
//  }
//
//  return Pair(sdf, nearest)
//}

//private fun sortOptions(items: List<Sdf>, hook: SdfHook, point: Vector3) =
//    items
//        .map { Pair(it, it(hook, point)) }
//        .sortedBy { it.second.value }

//fun arrangedPlus(sorted: List<Sdf>, lastDistance: Float): Sdf = { hook, point ->
//  val next = sorted.first()(hook, point)
//  if (next.value > lastDistance) {
//    if (sorted.size == 1)
//      rayMiss
//    else {
////      val current = sorted[1](hook, point)
//      val resorted = sortOptions(sorted.drop(1), hook, point)
//      val closest = resorted.first()
////      val j = sortOptions(sorted, hook, point)
////      val a = j.map {it.first}
////      val b = j.map {it.second}
////      val skip = sorted.indexOf(closest.first!!)
//      closest.second.copy(
//          nextSdf = arrangedPlus(resorted.map { it.first }, closest.second.value)
//      )
//    }
//  } else {
//    val resorted = sortOptions(sorted.drop(1), hook, point)
//    if (resorted.first().first != sorted.first()) {
//      val a = resorted.map { it.first }
//      val k = 0
//    }
//    next.copy(
//        nextSdf = arrangedPlus(sorted, next.value)
//    )
//  }
//}
//
//fun arrangedPlus(items: List<Sdf>): Sdf = { hook, point ->
//  val sorted = sortOptions(items, hook, point)
//  val first = sorted.first()
//  first.second.copy(
//      nextSdf = arrangedPlus(sorted.map { it.first }, first.second.value)
//  )
//}

//fun arrangedPlus2(items: List<Sdf>): Sdf {
//  val lastDistances: MutableList<Float?> = items.map { 100000f }.toMutableList()
//  return { hook, point ->
//    var nearest: PointDistance = rayMiss
//    var j = -1
//    items.forEachIndexed { i, item ->
//      val last = lastDistances[i]
////      if (last != null) {
//        val distance = item(hook, point)
//        if (distance.value < nearest.value) {
//          nearest = distance
//          j = i
//        }
//        lastDistances[i] = if (last == null || distance.value > last)
//          null
//        else
//          distance.value
////      }
//    }
//
//    if (lastDistances[j] == null) {
//      val k = 0
//    }
//    nearest
//  }
//}

//fun blend(range: Float, vararg items: Sdf): Sdf = { hook, point ->
//  val sorted = items.map { it(hook, point) }
//      .sortedBy { it.value }
//
//  if (sorted.size > 1) {
//    val a = sorted[0].value
//    val b = sorted[1].value
//    val gap = Math.abs(a - b)
//    val scale = Math.min(range, gap)
//    val curved = quadIn(scale)
////    val rangeValue = (range + Math.min(range, gap)) / 2f
//    val scaledRange = range + curved
//    val j = a + b - scaledRange
//    val c = listOf(a, j).sorted().first()
//    val h = a + b - range * 2f
//    if (point.x == 0f && point.z == 0f) {
////      if (point.x > -0.01f && point.x < 0.01f && point.z > -0.01f && point.z < 0.01f) {
////      println("${point.z} $a, $j, $gap")
//      println("${point.z} $h")
//    }
//    if (isRayHit(j)) {
//      val k = 0
//    }
////    println("${point.z} $h")
////    println(h)
//    PointDistance(diff(h, a))
//  } else
//    sorted.first()
//}
