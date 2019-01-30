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

fun applyCompression(buffer: FloatBuffer, min: Float, max: Float): FloatBuffer {
  val output = FloatBuffer.allocate(buffer.capacity())
  val breadth = max - min
  val mod = 1f / breadth

  fun normalize(value: Float) =
      quadOut(minMax((value - min) * mod, 0f, 1f))

  fun normalize1(value: Float) =
      (value - min) * mod

  fun normalize2(value: Float) =
      quadOut(minMax((value - min) * mod, 0f, 1f))

  loopColorBuffer(buffer) { color ->
    val n = (color - min) * mod
    
    output.put(normalize(color.x))
    output.put(normalize(color.y))
    output.put(normalize(color.z))
  }

  output.rewind()
  return output
}

fun compressRange(buffer: FloatBuffer): FloatBuffer {
  val (min, max) = getLuminanceRange(buffer)
  return applyCompression(buffer, min, max)
}