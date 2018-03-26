package mythic.spatial

import org.joml.*
import org.joml.Math.PI
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import org.joml.Matrix4fc.PROPERTY_AFFINE
import org.joml.Matrix4fc.PROPERTY_ORTHONORMAL

typealias Vector2 = org.joml.Vector2f
typealias Vector3 = org.joml.Vector3f
typealias Vector4 = org.joml.Vector4f
typealias Matrix = org.joml.Matrix4f
typealias Quaternion = org.joml.Quaternionf

//public class Vector4 {
//
//  var x: Float = 0f
//  var y: Float = 0f
//  var z: Float = 0f
//  var a: Float = 0f
//
//  constructor(r: Float, g: Float, b: Float, a: Float) {
//    this.x = r
//    this.y = g
//    this.z = b
//    this.a = a
//  }
//}
//
//operator fun Vector3.plusAssign(other: Vector3) {
//  add(other)
//}

operator fun Vector3.times(other: Matrix): Vector3 = mulDirection(other)
operator fun Vector2.times(other: Float): Vector2 = mul(other, Vector2())
operator fun Vector3.times(other: Float): Vector3 = mul(other, Vector3())
operator fun Vector3.times(other: Vector3): Vector3 = mul(other, Vector3())
//operator fun Quaternion.times(other: Vector3): Vector3 = transform(other)

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

//fun rayIntersectsLine(rayStart: Vector3, rayEnd: Vector3, lineStart: Vector3, lineEnd: Vector3): Boolean {
//  val rayDirection = rayEnd - rayStart
//  val v1 = rayStart - lineStart
//  val v2 = lineEnd - lineStart
//  val v3 = Vector(-rayDirection.y, rayDirection.x)
//
//  val dot = v2 * v3
//  if (Math.Abs(dot) < 0.000001)
//    return null
//
//  val t1 = Vector.CrossProduct(v2, v1) / dot
//  val t2 = v1 * v3 / dot
//
//  return if (t1 >= 0.0 && t2 >= 0.0 && t2 <= 1.0) t1 else null
//
//}

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

val epsilon = 0.00000001f

// If successful returns the closest point of intersection on the line.
fun rayIntersectsLine(p1: Vector3, p2: Vector3, p3: Vector3, p4: Vector3, maxGap: Float): Vector3? {
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
//  resultSegmentPoint1.x = p1.x + mua * p21.x
//  resultSegmentPoint1.y = p1.y + mua * p21.y
//  resultSegmentPoint1.z = p1.z + mua * p21.z
//  resultSegmentPoint2.x = p3.x + mub * p43.x
//  resultSegmentPoint2.y = p3.y + mub * p43.y
//  resultSegmentPoint2.z = p3.z + mub * p43.z

  if (result1.distance(result2) > maxGap)
    return null

//  val dir = p4 - p3
//  val middle = result2 - p3
//
//  if (middle.length() > dir.length() || middle.length() < 0f)
//    return null

  if (!isBetween(result2, p3, p4))
    return null
//  val firstLength = p3.length()
//  val secondLength = p4.length()
//  val middleLength = result2.length()
//
//  if (middleLength > firstLength) {
//    if (middleLength > secondLength)
//      return null
//  } else if (middleLength < secondLength) {
//    return null
//  }

    return result2
}

fun projectPointOntoLine(v: Vector2, u1: Vector2, u2: Vector2): Vector2 {
  val u = u2 - u1
  val relative = u * u.dot(v - u1) / u.dot(u)
  return relative + u1
}

fun atan(v: Vector2) = //if (v.x < 0)
//  Math.atan2(v.y.toDouble(), v.x.toDouble()).toFloat() - Pi
//else
    Math.atan2(v.y.toDouble(), v.x.toDouble()).toFloat()

fun getAngle(a: Vector2, b: Vector2): Float {
  val ad = atan(a)
  val bd = atan(b)
//  println("fn " + a + " " + b + " = " + ad + " - " + bd + " = " + (ad - bd))
  return bd - ad
}

//fun getAngle(a: Vector2, b: Vector2, c: Vector2): Float {
//  return getAngle(a - b, c - b)
//}

val Vector4.xyz: Vector3
  get() = Vector3(x, y, z)

fun Vector3.copy() = Vector3(this)

fun Vector3.transform(m: Matrix) = m.transform(Vector4(this, 1f)).xyz

fun getVector3Center(first: Vector3, second: Vector3) =
    first + (second - first) * 0.5f

fun getRotationMatrix(matrix: Matrix) =
    Matrix().rotation(matrix.getUnnormalizedRotation(Quaternion()))

fun Vector2.toVector2i() = Vector2i(x.toInt(), y.toInt())
fun Vector2i.toVector2() = Vector2(x.toFloat(), y.toFloat())

fun lookAt(from: Vector3, to: Vector3, up: Vector3, matrix: Matrix): Matrix {
  val dir = from - to
  val inverseDirLength = 1.0f / Math.sqrt((dir.x * dir.x + dir.y * dir.y + dir.z * dir.z).toDouble()).toFloat()
  dir.x *= inverseDirLength
  dir.y *= inverseDirLength
  dir.z *= inverseDirLength
  val leftX1 = up.y * dir.z - up.z * dir.y
  val leftY1 = up.z * dir.x - up.x * dir.z
  val leftZ1 = up.x * dir.y - up.y * dir.x
//  leftX = up.y * dir.z - up.z * dir.y
//  leftY = up.z * dir.x - up.x * dir.z
//  leftZ = up.x * dir.y - up.y * dir.x
  val invLeftLength = 1.0f / Math.sqrt((leftX1 * leftX1 + leftY1 * leftY1 + leftZ1 * leftZ1).toDouble()).toFloat()
  val leftX = leftX1 * invLeftLength
  val leftY = leftY1 * invLeftLength
  val leftZ = leftZ1 * invLeftLength
  val upnX = dir.y * leftZ - dir.z * leftY
  val upnY = dir.z * leftX - dir.x * leftZ
  val upnZ = dir.x * leftY - dir.y * leftX

  matrix._m00(leftX)
  matrix._m01(upnX)
  matrix._m02(dir.x)
  matrix._m03(0.0f)
  matrix._m10(leftY)
  matrix._m11(upnY)
  matrix._m12(dir.y)
  matrix._m13(0.0f)
  matrix._m20(leftZ)
  matrix._m21(upnZ)
  matrix._m22(dir.z)
  matrix._m23(0.0f)
  matrix._m30(-(leftX * from.x + leftY * from.y + leftZ * from.z))
  matrix._m31(-(upnX * from.x + upnY * from.y + upnZ * from.z))
  matrix._m32(-(dir.x * from.x + dir.y * from.y + dir.z * from.z))
  matrix._m33(1.0f)
  matrix.assume(PROPERTY_AFFINE.toInt() or PROPERTY_ORTHONORMAL.toInt())

  return matrix
}

fun lookAt2(from: Vector3, to: Vector3, up: Vector3, matrix: Matrix): Matrix {
  val dir = from - to
  val inverseDirLength = 1.0f / Math.sqrt((dir.x * dir.x + dir.y * dir.y + dir.z * dir.z).toDouble()).toFloat()
  dir.x *= inverseDirLength
  dir.y *= inverseDirLength
  dir.z *= inverseDirLength
  val leftX1 = up.z * dir.y - up.y * dir.z
  val leftY1 = up.y * dir.x - up.x * dir.y
  val leftZ1 = up.x * dir.z - up.z * dir.x
//  leftX = up.z * dir.y - up.y * dir.z
//  leftY = up.y * dir.x - up.x * dir.y
//  leftZ = up.x * dir.z - up.z * dir.x
  val invLeftLength = 1.0f / Math.sqrt((leftX1 * leftX1 + leftY1 * leftY1 + leftZ1 * leftZ1).toDouble()).toFloat()
  val leftX = leftX1 * invLeftLength
  val leftY = leftY1 * invLeftLength
  val leftZ = leftZ1 * invLeftLength
  val upnX = dir.z * leftZ - dir.y * leftY
  val upnY = dir.y * leftX - dir.x * leftZ
  val upnZ = dir.x * leftY - dir.z * leftX

  matrix._m00(leftX)
  matrix._m01(upnX)
  matrix._m02(dir.x)
  matrix._m03(0.0f)
  matrix._m10(leftZ)
  matrix._m11(upnZ)
  matrix._m12(dir.z)
  matrix._m13(0.0f)
  matrix._m20(leftY)
  matrix._m21(upnY)
  matrix._m22(dir.y)
  matrix._m23(0.0f)
  matrix._m30(-(leftX * from.x + leftZ * from.z + leftY * from.y))
  matrix._m31(-(upnX * from.x + upnZ * from.z + upnY * from.y))
  matrix._m32(-(dir.x * from.x + dir.z * from.z + dir.y * from.y))
  matrix._m33(1.0f)
  matrix.assume(PROPERTY_AFFINE.toInt() or PROPERTY_ORTHONORMAL.toInt())

  return matrix
}

/*
LMatrix4 LookAt( const LVector3& Eye, const LVector3& Center, const LVector3& Up )
{
    LMatrix4 Matrix;

    LVector3 X, Y, Z;
Create a new coordinate system:

    Z = Eye - Center;
    Z.Normalize();
    Y = Up;
    X = Y.Cross( Z );
Recompute Y = Z cross X:

    Y = Z.Cross( X );
The length of the cross product is equal target the area of the parallelogram, which is < 1.0 for non-perpendicular unit-length vectors; so normalize X, Y here:

    X.Normalize();
    Y.Normalize();

 */

//fun switchYZ(v: Vector3) = Vector3(v.x, v.z, v.y)

fun lookAt3(eye: Vector3, target: Vector3, up: Vector3, matrix: Matrix): Matrix {
  val Z = (eye - target).normalize()
  val X = -Vector3(up).cross(Z).normalize()
  val Y = -Vector3(Z).cross(X).normalize()

  matrix._m00(X.x)
  matrix._m10(X.y)
  matrix._m20(X.z)

  matrix._m01(Y.x)
  matrix._m11(Y.y)
  matrix._m21(Y.z)

  matrix._m02(Z.x)
  matrix._m12(Z.y)
  matrix._m22(Z.z)

  matrix._m03(0.0f)
  matrix._m13(0.0f)
  matrix._m23(0.0f)

  matrix._m30(-X.dot(eye))
  matrix._m31(-Y.dot(eye))
  matrix._m32(-Z.dot(eye))
  matrix._m33(1.0f)
  matrix.assume(PROPERTY_AFFINE.toInt() or PROPERTY_ORTHONORMAL.toInt())
//  matrix.invertAffine()

//  val correction = Matrix()
//  correction._m11(0f)
//  correction._m12(1f)
//  correction._m22(0f)
//  correction._m21(-1f)
  return matrix
//  return Matrix() *
//      Matrix().translate(-X.dot(eye), -Y.dot(eye), -Z.dot(eye)) *
//      matrix *
//      Matrix().rotateX(-Pi / 2) *
//      Matrix()
}

//  val dir = (from - to)
//  val inverseDirLength = 1.0f / Math.sqrt((dir.x * dir.x + dir.y * dir.y + dir.z * dir.z).toDouble()).toFloat()
//  dir.x *= inverseDirLength
//  dir.y *= inverseDirLength
//  dir.z *= inverseDirLength
//  val leftX1 = up.z * dir.y - up.y * dir.z
//  val leftY1 = up.x * dir.z - up.z * dir.x
//  val leftZ1 = up.y * dir.x - up.x * dir.y
////  leftX = up.z * dir.y - up.y * dir.z
////  leftY = up.y * dir.x - up.x * dir.y
////  leftZ = up.x * dir.z - up.z * dir.x
//  val invLeftLength = 1.0f / Math.sqrt((leftX1 * leftX1 + leftY1 * leftY1 + leftZ1 * leftZ1).toDouble()).toFloat()
//  val leftX = leftX1 * invLeftLength
//  val leftY = leftY1 * invLeftLength
//  val leftZ = leftZ1 * invLeftLength
//  val upnX = dir.z * leftZ - dir.y * leftY
//  val upnY = dir.y * leftX - dir.x * leftZ
//  val upnZ = dir.x * leftY - dir.z * leftX