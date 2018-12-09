package mythic.spatial

fun quadOut(t: Float, b: Float, c: Float, d: Float): Float {
  val t2 = t / d
  return -c * t2 * (t2 - 2) + b
}

fun quadOut(t: Float): Float {
  return t * (2 - t)
}

fun cubicOut(t: Float): Float {
  return t * (2 - t)
}