package mythic.spatial

import org.joml.*
import org.joml.Math.PI
import java.nio.ByteBuffer
import java.nio.FloatBuffer

typealias Vector2 = org.joml.Vector2f
typealias Vector3 = org.joml.Vector3f
typealias Vector4 = org.joml.Vector4f
typealias Matrix = org.joml.Matrix4f
typealias Quaternion = org.joml.Quaternionf

operator fun Vector3.times(other: Matrix): Vector3 = mulDirection(other)
operator fun Vector2.times(other: Float): Vector2 = mul(other, Vector2())
operator fun Vector3.times(other: Float): Vector3 = mul(other, Vector3())
operator fun Vector3.times(other: Vector3): Vector3 = mul(other, Vector3())

fun FloatBuffer.put(value: Vector3) {
  put(value.x)
  put(value.y)
  put(value.z)
}

fun FloatBuffer.put(value: Vector4) {
  put(value.x)
  put(value.y)
  put(value.z)
  put(value.w)
}

fun ByteBuffer.putVector3(value: Vector3) {
  putFloat(value.x)
  putFloat(value.y)
  putFloat(value.z)
}

fun ByteBuffer.putVector4(value: Vector4) {
  putFloat(value.x)
  putFloat(value.y)
  putFloat(value.z)
  putFloat(value.w)
}

val Pi = PI.toFloat()

fun Vector2.toVector3() = Vector3(x, y, 0f)

data class BoundingBox(
    val start: Vector3,
    val end: Vector3
) {
  val dimensions: Vector3
    get() = end - start
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

fun lineIntersectsSphere(lineStart: Vector3, lineEnd: Vector3, circleCenter: Vector3, radius: Float): Boolean {
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

fun rayIntersectsSphere(lineStart: Vector3, lineEnd: Vector3, circleCenter: Vector3, radius: Float): Boolean {
  val d = lineEnd - lineStart
  val f = lineStart - circleCenter
  val a = d.dot(d)
  val b = 2f * f.dot(d)
  val c = f.dot(f) - radius * radius

  var discriminant = b * b - 4f * a * c
  if (discriminant < 0) {
    return false
  } else {
    return true
  }
}

fun isBetween(middle: Float, first: Float, second: Float) =
    if (middle == first || middle == second)
      true
    else if (middle > first)
      middle < second
    else
      middle > second

fun isBetween(middle: Vector3, first: Vector3, second: Vector3) =
    isBetween(middle.x, first.x, second.x)
        && isBetween(middle.y, first.y, second.y)
        && isBetween(middle.z, first.z, second.z)

fun isBetween(middle: Vector2, first: Vector2, second: Vector2) =
    isBetween(middle.x, first.x, second.x)
        && isBetween(middle.y, first.y, second.y)

val epsilon = 0.00000001f

// If successful returns the closest point of intersection on the line.
fun rayIntersectsLine3D(p1: Vector3, p2: Vector3, p3: Vector3, p4: Vector3, maxGap: Float): Vector3? {
  val p13 = p1 - p3
  val p43 = p4 - p3

  if (p43.lengthSquared() < epsilon) {
    return null
  }
  val p21 = p2 - p1
  if (p21.lengthSquared() < epsilon) {
    return null
  }

  val d1343 = p13.x * p43.x + p13.y * p43.y + p13.z * p43.z
  val d4321 = p43.x * p21.x + p43.y * p21.y + p43.z * p21.z
  val d1321 = p13.x * p21.x + p13.y * p21.y + p13.z * p21.z
  val d4343 = p43.x * p43.x + p43.y * p43.y + p43.z * p43.z
  val d2121 = p21.x * p21.x + p21.y * p21.y + p21.z * p21.z

  val denom = d2121 * d4343 - d4321 * d4321
  if (Math.abs(denom) < epsilon) {
    return null
  }
  val numer = d1343 * d4321 - d1321 * d4343

  val mua = numer / denom
  val mub = (d1343 + d4321 * mua) / d4343

  val result1 = p1 + p21 * mua
  val result2 = p3 + p43 * mub

  if (result1.distance(result2) > maxGap)
    return null

  if (!isBetween(result2, p3, p4))
    return null

  return result2
}

fun rayPolygonDistance(rayStart: Vector3, rayDirection: Vector3, polygonPoint: Vector3, polygonNormal: Vector3): Float? {
  val denominator = polygonNormal.dot(rayDirection)
  if (denominator < 1e-6)
    return null

  val foo = polygonPoint - rayStart
  val T = foo.dot(polygonNormal) / denominator
  return if (T >= 0f)
    T
  else
    null
}

private val flattenedPlaneNormal = Vector3(0f, 0f, 1f)

fun getSlope(start: Vector2, end: Vector2): Float {
  val normal = end - start
  return normal.y / normal.x
}

fun simpleRayIntersectsLineSegment(rayStart: Vector2, segmentStart: Vector2, segmentEnd: Vector2): Vector2? {
  if (segmentStart.y == segmentEnd.y)
    return null

  if (!isBetween(rayStart.y, segmentStart.y, segmentEnd.y))
    return null

  val x = if (segmentStart.x == segmentEnd.x) {
    segmentStart.x
  } else {
    val slope = getSlope(segmentStart, segmentEnd)
    (rayStart.y - segmentStart.y) / slope + segmentStart.x
  }

  return if (x >= rayStart.x && isBetween(x, segmentStart.x, segmentEnd.x))
    Vector2(x, rayStart.y)
  else
    null
}

fun simpleRayIntersectsLineSegmentAsNumber(rayStart: Vector2, segmentStart: Vector2, segmentEnd: Vector2): Int {
  val result = simpleRayIntersectsLineSegment(rayStart, segmentStart, segmentEnd)
  return if (result != null) 1 else 0
}

fun isEven(value: Int) = (value and 1) == 0

fun isOdd(value: Int) = (value and 1) != 0

fun isInsidePolygon(point: Vector2, vertices: List<Vector2>): Boolean {
  var count = simpleRayIntersectsLineSegmentAsNumber(point, vertices.last(), vertices.first())
  for (i in 0 until vertices.size - 1) {
    count += simpleRayIntersectsLineSegmentAsNumber(point, vertices[i], vertices[i + 1])
  }

  return count > 0 && isOdd(count)
}

fun rayIntersectsPolygon3D(rayStart: Vector3, rayDirection: Vector3, vertices: List<Vector3>, polygonNormal: Vector3): Vector3? {
  val distance = rayPolygonDistance(rayStart, rayDirection, vertices.first(), polygonNormal)
  if (distance == null)
    return null

  val planeIntersection = rayStart + rayDirection * distance

//  val polygonRotation = Quaternion().rotateTo(polygonNormal, flattenedPlaneNormal)
//  val transformedPoints = vertices.map { (polygonRotation * it).xy }
//  val transformedIntersection = (polygonRotation * planeIntersection).xy
//  val u = Vector3(polygonNormal.y, -polygonNormal.x, 0f)
  val u = Vector3(polygonNormal).cross((vertices[1] - vertices[0]).normalize())
  val v = Vector3(polygonNormal).cross(u)
  val transformedPoints = vertices.map { Vector2(u.dot(it), v.dot(it)) }
  val transformedIntersection = Vector2(u.dot(planeIntersection), v.dot(planeIntersection))

  return if (isInsidePolygon(transformedIntersection, transformedPoints))
    planeIntersection
  else
    null

  // *** Test code ***
//  val temp1 = Vector3(-1f, -3f, 3f)
//  val temp2 = Vector3(-1f, -2f, 2f)
//  val na = Vector3(1f, 0f, 0f)
//  val nb = Vector3(0f, -1f, 0f)
//  val quat = Quaternion().rotateTo(na, nb)
//  val r1 = quat * temp1
//  val r2 = quat * temp2
}

fun projectPointOntoRay(v: Vector2, u1: Vector2, u2: Vector2): Vector2 {
  val u = u2 - u1
  val relative = u * u.dot(v - u1) / u.dot(u)
  return relative + u1
}

fun projectPointOntoLine(v: Vector2, u1: Vector2, u2: Vector2): Vector2? {
  val u = u2 - u1
  val relative = u * u.dot(v - u1) / u.dot(u)
  val result = relative + u1
  if (isBetween(result, u1, u2))
    return result
  else
    return null
}

fun atan(v: Vector2) = //if (v.x < 0)
//  Math.atan2(v.y.toDouble(), v.x.toDouble()).toFloat() - Pi
//else
    Math.atan2(v.y.toDouble(), v.x.toDouble()).toFloat()

fun getAngle(a: Vector2, b: Vector2): Float {
  val ad = atan(a)
  val bd = atan(b)
//  println("fn " + a + " " + b + " = " + ad + " - " + bd + " = " + (ad - bd))
  if (bd - ad == Float.NaN)
    println("heyllo")
  return bd - ad
}

//fun getAngle(a: Vector2, b: Vector2, c: Vector2): Float {
//  return getAngle(a - b, c - b)
//}

val Vector4.xyz: Vector3
  get() = Vector3(x, y, z)

//val Vector4.xy: Vector2
//  get() = Vector2(x, y)

fun Vector2.copy() = Vector2(this)
fun Vector3.copy() = Vector3(this)

fun Vector3.transform(m: Matrix) = m.transform(Vector4(this, 1f)).xyz
//fun Vector2.transform(m: Matrix) = m.transform(Vector4(x, y, 0f, 1f)).xy

fun getVector3Center(first: Vector3, second: Vector3) =
    first + (second - first) * 0.5f

fun getRotationMatrix(matrix: Matrix) =
    Matrix().rotation(matrix.getUnnormalizedRotation(Quaternion()))

fun Vector2.toVector2i() = Vector2i(x.toInt(), y.toInt())
fun Vector2i.toVector2() = Vector2(x.toFloat(), y.toFloat())

fun Vector4i.toVector4() = Vector4(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())

fun clockwiseLesser(center: Vector3, a: Vector3, b: Vector3): Boolean {
  val ax = a.x - center.x
  val bx = b.x - center.x
  
  if (ax >= 0f && bx < 0f)
    return true
  if (ax < 0f && bx >= 0f)
    return false

  val ay = a.y - center.y
  val by = b.y - center.y

  if (ax == 0f && bx == 0f) {
    return if (ay >= 0 || by >= 0f) a.y > b.y else b.y > a.y
  }

  // compute the cross product of vectors (center -> a) x (center -> b)
  val det = ax * by - bx * ay
  if (det < 0)
    return true
  if (det > 0)
    return false

  // points a and b are on the same line from the center
  // check which point is closer to the center
  val d1 = ax * ax + ay * ay
  val d2 = bx * bx + by * by
  return d1 > d2
}

fun counterClockwiseLesser(center: Vector3) = { a: Vector3, b: Vector3 ->
  !clockwiseLesser(center, a, b)
}

fun arrangePointsCounterClockwise(center: Vector3, vertices: List<Vector3>): List<Vector3> {
  val sorter = counterClockwiseLesser(center)
  return vertices.sortedWith(object : Comparator<Vector3> {
    override fun compare(a: Vector3, b: Vector3): Int =
        if (sorter(a, b)) 1 else 0
  })
}

fun getCenter(points: List<Vector3>): Vector3 =
    points.reduce { a, b -> a + b } / points.size.toFloat()

fun arrangePointsCounterClockwise(vertices: List<Vector3>): List<Vector3> =
    arrangePointsCounterClockwise(getCenter(vertices), vertices)
