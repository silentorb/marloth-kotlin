package mythic.spatial

fun quadOut(t: Float, b: Float, c: Float, d: Float): Float {
  val t2 = t / d
  return -c * t2 * (t2 - 2) + b
}

fun quadOut(t: Float): Float {
  return t * (2 - t)
}

fun quadIn(t: Float): Float = t * t

fun cubicIn(t: Float): Float = t * t * t

fun cubicOut(t: Float): Float {
  val n = t - 1
  return n * n * n + 1
}

fun interpolate(scalar: Float, a: Vector3, b: Vector3): Vector3 =
    b * scalar + a * (1f - scalar)

fun interpolate(scalar: Float, a: Quaternion, b: Quaternion): Quaternion =
    Quaternion(a).slerp(b, scalar)