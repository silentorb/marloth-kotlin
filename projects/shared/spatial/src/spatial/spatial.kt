package spatial

typealias Vector3 = com.badlogic.gdx.math.Vector3
typealias Matrix = com.badlogic.gdx.math.Matrix4
typealias Vector2 = com.badlogic.gdx.math.Vector2

operator fun Vector3.plusAssign(other: Vector3) {
  add(other)
}

operator fun Vector3.times(other: Matrix): Vector3 {
  return mul(other)
}

operator fun Vector2.times(other: Float): Vector2 {
  return scl(other)
}