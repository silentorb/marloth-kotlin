package generation

import mythic.sculpting.HalfEdgeMesh
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.times
import mythic.spatial.toVector3
import org.joml.minus
import org.joml.plus

fun <T> divide(sequence: Sequence<T>, filter: (T) -> Boolean) =
    Pair(sequence.filter(filter), sequence.filter { !filter(it) })

fun <T> divide(sequence: List<T>, filter: (T) -> Boolean) =
    Pair(sequence.filter(filter), sequence.filter { !filter(it) })

fun getNodeDistance(first: Node, second: Node): Float =
    first.position.distance(second.position) - first.radius - second.radius

fun overlaps2D(first: Node, second: Node): Boolean {
  val distance = getNodeDistance(first, second)
  return distance < 0
}

fun lineIntersectsCircle(lineStart: Vector2, lineEnd: Vector2, circleCenter: Vector2, radius: Float): Boolean {
  val d = lineEnd - lineStart
  val f = lineStart - circleCenter
  val a = d.dot(d)
  val b = 2f * f.dot(d)
  val c = f.dot(f) - radius * radius

  var discriminant = b * b - 4f * a * c
  if (discriminant < 0) {
    return false
  } else {
    // ray didn't totally miss sphere,
    // so there is a solution to
    // the equation.

    discriminant = Math.sqrt(discriminant.toDouble()).toFloat()

    // either solution may be on or off the ray so need to test both
    // t1 is always the smaller value, because BOTH discriminant and
    // a are nonnegative.
    val t1 = (-b - discriminant) / (2 * a)
    val t2 = (-b + discriminant) / (2 * a)

    // 3x HIT cases:
    //          -o->             --|-->  |            |  --|->
    // Impale(t1 hit,t2 hit), Poke(t1 hit,t2>1), ExitWound(t1<0, t2 hit),

    // 3x MISS cases:
    //       ->  o                     o ->              | -> |
    // FallShort (t1>1,t2>1), Past (t1<0,t2<0), CompletelyInside(t1<0, t2>1)

    if (t1 >= 0 && t1 <= 1) {
      // t1 is the intersection, and it's closer than t2
      // (since t1 uses -b - discriminant)
      // Impale, Poke
      return true
    }

    // here t1 didn't intersect so we are either started
    // inside the sphere or completely past it
    return if (t2 >= 0 && t2 <= 1) {
      // ExitWound
      true
    } else false

    // no intn: FallShort, Past, CompletelyInside
  }
}

fun forkVector(point: Vector2, direction: Vector2, length: Float): List<Vector2> {
  val perpendicular = Vector2(direction.y, -direction.x) * length
  return listOf(point + perpendicular, point - perpendicular)
}

fun circleIntersection(aPoint: Vector2, aRadius: Float, bPoint: Vector2, bRadius: Float): List<Vector2> {
  val distance = aPoint.distance(bPoint)
  val aLength = (aRadius * aRadius - bRadius * bRadius + distance * distance) / (distance * 2)
  val direction = (bPoint - aPoint).normalize()
  val center = aPoint + direction.normalize() * aLength
  val pLength = Math.sqrt((aRadius * aRadius - aLength * aLength).toDouble()).toFloat()
  return forkVector(center, direction, pLength)
}

fun getAngle(first: Vector2, second: Vector2): Float {
  val third = second - first
  return Math.atan2(third.y.toDouble(), third.x.toDouble()).toFloat()
}

fun project2D(angle: Float, distance: Float): Vector2 {
  return Vector2(
      Math.cos(angle.toDouble()).toFloat() * distance,
      Math.sin(angle.toDouble()).toFloat() * distance
  )
}