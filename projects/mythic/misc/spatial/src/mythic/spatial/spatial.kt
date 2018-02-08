package mythic.spatial

import org.joml.Math.PI
import org.joml.div
import org.joml.minus
import org.joml.plus
import java.nio.FloatBuffer

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
  return ad - bd
}

//fun getAngle(a: Vector2, b: Vector2, c: Vector2): Float {
//  return getAngle(a - b, c - b)
//}

val Vector4.xyz: Vector3
  get() = Vector3(x, y, z)

fun Vector3.transform(m: Matrix) = m.transform(Vector4(this, 1f)).xyz