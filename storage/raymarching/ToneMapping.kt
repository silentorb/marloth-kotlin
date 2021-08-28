package silentorb.raymarching

import silentorb.mythic.spatial.*
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

fun getBiggest(v: Vector3): Float =
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

  loopFloatBuffer(buffer) { color ->
    val n = (color - min) * mod
    output.put(cleanup(n))
  }

  output.rewind()
  return output
}

fun compressRange(buffer: FloatBuffer): FloatBuffer {
  val (min, max) = getLuminanceRange(buffer)
  return applyCompression(buffer, min, max)
}
