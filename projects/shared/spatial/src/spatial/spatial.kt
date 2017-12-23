package spatial

typealias Vector2 = com.badlogic.gdx.math.Vector2
typealias Vector3 = com.badlogic.gdx.math.Vector3
typealias Matrix = com.badlogic.gdx.math.Matrix4

public class Vector4 {

  var r: Float = 0f
  var g: Float = 0f
  var b: Float = 0f
  var a: Float = 0f

  constructor(r: Float, g: Float, b: Float, a: Float) {
    this.r = r
    this.g = g
    this.b = b
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