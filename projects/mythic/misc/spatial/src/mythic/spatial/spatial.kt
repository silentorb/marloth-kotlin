package mythic.spatial

import org.joml.Math.PI
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
operator fun Vector3.plusAssign(other: Vector3) {
  add(other)
}

operator fun Vector3.times(other: Matrix): Vector3 = mulDirection(other)
operator fun Vector2.times(other: Float): Vector2 = mul(other)
operator fun Vector3.times(other: Float): Vector3 = mul(other)
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