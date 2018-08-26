package mythic.spatial

import org.joml.*
import org.joml.Math.PI
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.text.DecimalFormat

typealias Vector2 = org.joml.Vector2f
typealias Vector4 = org.joml.Vector4f
typealias Matrix = org.joml.Matrix4f
typealias Quaternion = org.joml.Quaternionf

private val initialQuaternion = Quaternion()

operator fun Vector3.times(other: Matrix): Vector3 = mulDirection(other)
operator fun Vector2.times(other: Vector2): Vector2 = mul(other, Vector2())
operator fun Vector2.times(other: Float): Vector2 = mul(other, Vector2())
operator fun Vector3.times(other: Float): Vector3 = mul(other, Vector3())
operator fun Vector3.times(other: Vector3): Vector3 = mul(other, Vector3())

//operator fun Matrix.times(other: Matrix): Matrix = Matrix(this).mul(other)

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

fun FloatBuffer.put(x: Float, y: Float, z: Float, w: Float) {
  put(x)
  put(y)
  put(z)
  put(w)
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

fun ByteBuffer.putMatrix(value: Matrix) {
  value.get(this)
  position(position() + 16 * 4)
}

const val Pi = PI.toFloat()
const val Pi2 = Pi * 2f

fun Vector2.toVector3() = Vector3(x, y, 0f)

data class BoundingBox(
    val start: Vector3,
    val end: Vector3
) {
  val dimensions: Vector3
    get() = end - start
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

fun isBetween(middle: Vector2fMinimal, first: Vector2fMinimal, second: Vector2fMinimal) =
    isBetween(middle.x, first.x, second.x)
        && isBetween(middle.y, first.y, second.y)

val epsilon = 0.00000001f

fun rayPolygonDistance(rayStart: Vector3, rayDirection: Vector3, polygonPoint: Vector3, polygonNormal: Vector3): Float? {
  val denominator = -polygonNormal.dot(rayDirection)
  if (denominator < 1e-6)
    return null

  val foo = polygonPoint - rayStart
  val T = foo.dot(polygonNormal) / denominator
  return if (T <= 0f)
    -T
  else
    null
}

private val flattenedPlaneNormal = Vector3(0f, 0f, 1f)

fun getSlope(start: Vector2fMinimal, end: Vector2fMinimal): Float {
  val normal = end - start
  return normal.y / normal.x
}

fun isEven(value: Int) = (value and 1) == 0

fun isOdd(value: Int) = (value and 1) != 0

fun isInsidePolygon(point: Vector2fMinimal, vertices: List<Vector2fMinimal>): Boolean {
  var count = simpleRayIntersectsLineSegmentAsNumber(point, vertices.last(), vertices.first())
  for (i in 0 until vertices.size - 1) {
    count += simpleRayIntersectsLineSegmentAsNumber(point, vertices[i], vertices[i + 1])
  }

  return count > 0 && isOdd(count)
}

fun flattenPoints(normal: Vector3, points: List<Vector3>): Map<Vector2, Vector3> {
  val u = Vector3(normal).cross((points[1] - points[0]).normalize())
  val v = Vector3(normal).cross(u)
  return points.associate { Pair(Vector2(u.dot(it), v.dot(it)), it) }
}

fun flattenPoints(points: List<Vector3>): Map<Vector2, Vector3> {
  assert(points.size >= 3)
  val normal = (points[0] - points[1]).cross(points[2] - points[1])
  return flattenPoints(normal, points)
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

fun Vector4.xy(): Vector2 = Vector2(x, y)

val Vector4.zw: Vector2
  get() = Vector2(z, w)

//val Vector4.xy(): Vector2
//  get() = Vector2(x, y)

fun Vector2.copy() = Vector2(this)
fun Vector3.copy() = Vector3(this)

fun Vector3.transform(m: Matrix) = m.transform(Vector4(this, 1f)).xyz

private val tempVector = Vector4()
fun transformVector(m: Matrix): Vector3 {
  tempVector.set(0f, 0f, 0f, 1f)
  return m.transform(tempVector).xyz
}

//fun Vector2.transform(m: Matrix) = m.transform(Vector4(x, y, 0f, 1f)).xy()

fun getVector3Center(first: Vector3, second: Vector3) =
    first + (second - first) * 0.5f

fun getRotationMatrix(matrix: Matrix) =
    Matrix().rotation(matrix.getUnnormalizedRotation(initialQuaternion))

fun Vector2.toVector2i() = Vector2i(x.toInt(), y.toInt())
fun Vector2i.toVector2() = Vector2(x.toFloat(), y.toFloat())

fun Vector4i.toVector4() = Vector4(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())

fun arrangePointsCounterClockwise2D(center: Vector2, vertices: Collection<Vector2>): List<Vector2> =
    vertices.sortedBy { atan(it - center) }

fun getCenter(points: Collection<Vector3>): Vector3 =
    points.reduce { a, b -> a + b } / points.size.toFloat()

fun getCenter2D(points: Collection<Vector2>): Vector2 =
    points.reduce { a, b -> a + b } / points.size.toFloat()

fun arrangePointsCounterClockwise2D(vertices: List<Vector2>): List<Vector2> =
    arrangePointsCounterClockwise2D(getCenter2D(vertices), vertices)

fun arrangePointsCounterClockwise(vertices: List<Vector3>): List<Vector3> {
  val flatMap = flattenPoints(vertices)
  val flatVertices = flatMap.keys
  val sorted = arrangePointsCounterClockwise2D(getCenter2D(flatVertices), flatVertices)
  val result = sorted.map { flatMap[it]!! }
  return result
}

val decimalFormat = DecimalFormat("#.#####")
fun toString(vector: Vector3) =
    decimalFormat.format(vector.x) + ", " + decimalFormat.format(vector.y) + ", " + decimalFormat.format(vector.z)

fun toString(vectors: List<Vector3>) =
    vectors.map { toString(it) }.joinToString("\n")

fun toString2(vector: Vector2) =
    decimalFormat.format(vector.x) + ", " + decimalFormat.format(vector.y)

fun toString2(vectors: List<Vector2>) =
    vectors.map { toString2(it) }.joinToString("\n")

fun isZero(vector: Vector3) =
    vector.x == 0f && vector.y == 0f && vector.z == 0f

fun rotateToward(matrix: Matrix, dir: Vector3): Matrix =
    if (dir.x == 0f && dir.y == 0f)
      matrix.rotateTowards(dir, Vector3(0f, 1f, 0f))
    else
      matrix.rotateTowards(dir, Vector3(0f, 0f, 1f))

fun rotateToward(dir: Vector3): Quaternion =
//    if (dir.x == 0f && dir.y == 0f)
    Quaternion().rotateTo(Vector3(1f, 0f, 0f), dir)
//    else
//      Quaternion().rotateTo(dir, Vector3(0f, 0f, 1f))

fun sum(vertices: Collection<Vector3>): Vector3 {
  var result = Vector3()
  for (vertex in vertices) {
    result += vertex
  }

  return result / vertices.size.toFloat()
}

fun minMax(value: Float, min: Float, max: Float): Float =
    if (value < min)
      min
    else if (value > max)
      max
    else
      value