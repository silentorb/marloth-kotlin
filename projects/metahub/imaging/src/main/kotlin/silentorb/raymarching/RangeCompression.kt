package silentorb.raymarching

import mythic.spatial.*
import java.nio.FloatBuffer

private val flatConversion = Vector3(0.3333f, 0.3333f, 0.3333f)

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

fun getLuminanceRange(buffer: FloatBuffer): Pair<Float, Float> {
  var min = 1f
  var max = 0f

  loopColorBuffer(buffer) { color ->
    val luminance = getLuminance(flatConversion, color)
    if (luminance < min)
      min = luminance
    else if (luminance > max)
      max = luminance
  }

  return Pair(min, max)
}

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

  fun mix(a: Float, b: Float):Float =
      Math.min(1f, cleanup(a) * 0.3f + b * 0.7f)

  loopColorBuffer(buffer) { color ->
    val n = Vector3((color - min) * mod)
    val n2 = if (n.x > 1f || n.y > 1f || n.z > 1f) {
      val j = listOf(n.x, n.y, n.z)
      val spillOver = j.map { if (it > 1f) it - 1f else 0f }.sum() / 3
      val m = j.map {
        if (it > 1f)
          1f
        else {
          Math.min(1f, it + spillOver)
        }
      }
      Vector3(m[0], m[1], m[2])
    } else
      n

//    output.put(mix(n2.x, color.x))
//    output.put(mix(n2.y, color.y))
//    output.put(mix(n2.y, color.z))

    output.put(cleanup(n2.x))
    output.put(cleanup(n2.y))
    output.put(cleanup(n2.y))
  }

  output.rewind()
  return output
}

fun compressRange(buffer: FloatBuffer): FloatBuffer {
  val (min, max) = getLuminanceRange(buffer)
  return applyCompression(buffer, min, max)
}