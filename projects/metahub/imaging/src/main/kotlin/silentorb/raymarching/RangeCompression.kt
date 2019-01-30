package silentorb.raymarching

import mythic.spatial.*
import java.nio.FloatBuffer

fun getLuminance(rates: Vector3, color: Vector3): Float =
    color.x * rates.x + color.y * rates.y + color.z * rates.z

fun loopColorBuffer(buffer: FloatBuffer, action: (Vector3) -> Unit) {
  buffer.rewind()
  val count = buffer.capacity() / 3
  for (i in 0 until count) {
    val color = Vector3(buffer.get(), buffer.get(), buffer.get())
    action(color)
  }
}

fun loopFloatBuffer(buffer: FloatBuffer, action: (Float) -> Unit) {
  buffer.rewind()
  val count = buffer.capacity()
  for (i in 0 until count) {
    action(buffer.get())
  }
}

fun getLuminanceRange(buffer: FloatBuffer): Pair<Float, Float> {
  var min = 1f
  var max = 0f

  fun update(luminance: Float) {
    if (luminance < min)
      min = luminance
    else if (luminance > max)
      max = luminance
  }
  loopFloatBuffer(buffer, ::update)
//  buffer.rewind()
//  val count = buffer.capacity()
//  for (i in 0 until count) {
//    update(buffer.get())
//  }

//  loopColorBuffer(buffer) { color ->
//    update(color.x)
//    update(color.y)
//    update(color.z)
//  }

  return Pair(min, max)
}

//fun getChannelLuminanceRange(buffer: FloatBuffer): List<Pair<Float, Float>> {
//  val ranges = (1..3).map { mutableListOf(1f, 0f) }
//
//  fun updateRange(index: Int, luminance: Float) {
//    val range = ranges[index]
//    if (luminance < range[0])
//      range[0] = luminance
//    else if (luminance > range[1])
//      range[1] = luminance
//  }
//
//  for (value in buffer.array()) {
//    updateRange()
//  }
////  loopColorBuffer(buffer) { color ->
////    updateRange(0, color.x)
////    updateRange(1, color.y)
////    updateRange(2, color.z)
////  }
//
//  return ranges.map { Pair(it[0], it[1]) }
//}

fun getLargest(v: Vector3): Float =
    if (v.x > v.y)
      if (v.x > v.z)
        v.x
      else
        v.z
    else if (v.y > v.z)
      v.y
    else
      v.z

fun getSmallest(v: Vector3): Float =
    if (v.x < v.y)
      if (v.x < v.z)
        v.x
      else
        v.z
    else if (v.y < v.z)
      v.y
    else
      v.z

fun applyCompression(buffer: FloatBuffer, min: Float, max: Float): FloatBuffer {
  val output = FloatBuffer.allocate(buffer.capacity())
  val breadth = max - min
  val mod = 1f / breadth

  fun cleanup(value: Float) =
      minMax(value, 0f, 1f)

//  fun normalize1(value: Float) =
//      (value - min) * mod

//  fun normalize2(value: Float) =
//      quadOut(minMax((value - min) * mod, 0f, 1f))

  fun mix(a: Float, b: Float): Float =
      Math.min(1f, cleanup(a) * 0.3f + b * 0.7f)

  fun floor(value: Float) = Math.max(0f, value)

  loopFloatBuffer(buffer) { color ->
    //    val bottom = getSmallest(color)
//    val floored = Vector3(floor(color.x), floor(color.y), floor(color.z))
    val n = color + min * mod
    val n2 = n / (n + 1f)
//    val n2 = if (n.x > 1f || n.y > 1f || n.z > 1f) {
//      val j = listOf(n.x, n.y, n.z)
//      val spillOver = j.map { if (it > 1f) it - 1f else 0f }.sum() / 3
//      val m = j.map {
//        if (it > 1f)
//          1f
//        else {
//          Math.min(1f, it + spillOver)
//        }
//      }
//      Vector3(m[0], m[1], m[2])
//    } else
//      n

//    output.put(mix(n2.x, color.x))
//    output.put(mix(n2.y, color.y))
//    output.put(mix(n2.y, color.z))

    output.put(cleanup(n2))
//    output.put(cleanup(n2.y))
//    output.put(cleanup(n2.y))
  }

  output.rewind()
  return output
}

fun compressRange(buffer: FloatBuffer): FloatBuffer {
  val (min, max) = getLuminanceRange(buffer)
  return applyCompression(buffer, min, max)
//  val ranges = getChannelLuminanceRange(buffer)
//  return applyCompression(buffer, ranges)
}