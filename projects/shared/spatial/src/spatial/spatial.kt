package spatial

import java.nio.FloatBuffer

typealias Vector2 = com.badlogic.gdx.math.Vector2
typealias Vector3 = com.badlogic.gdx.math.Vector3
typealias Matrix = com.badlogic.gdx.math.Matrix4

public class Vector4 {

  var x: Float = 0f
  var y: Float = 0f
  var z: Float = 0f
  var a: Float = 0f

  constructor(r: Float, g: Float, b: Float, a: Float) {
    this.x = r
    this.y = g
    this.z = b
    this.a = a
  }
}

operator fun Vector3.plusAssign(other: Vector3) {
  add(other)
}

operator fun Vector3.times(other: Matrix): Vector3 {
  return mul(other)
}

operator fun Vector2.times(other: Float): Vector2 {
  return scl(other)
}

operator fun Vector3.times(other: Float): Vector3 {
  return scl(other)
}

fun FloatBuffer.put(value: Vector3) {
  put(value.x)
  put(value.y)
  put(value.z)
}

fun FloatBuffer.put(value: Vector4) {
  put(value.x)
  put(value.y)
  put(value.z)
  put(value.a)
}