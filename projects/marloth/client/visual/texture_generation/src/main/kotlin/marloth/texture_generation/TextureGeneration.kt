package marloth.texture_generation

import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.put
import mythic.spatial.times
import org.joml.plus
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

typealias OpaqueColor = mythic.spatial.Vector3
typealias TransparentColor = mythic.spatial.Vector4

typealias TextureAlgorithm<T> = (x: Float, y: Float) -> T

typealias ScalarTextureAlgorithm = TextureAlgorithm<Float>
typealias OpaqueTextureAlgorithm = TextureAlgorithm<OpaqueColor>
typealias TransparentTextureAlgorithm = TextureAlgorithm<TransparentColor>

fun flip(a: Boolean, b: Boolean) = if (b) a else !a

fun solidColor(color: OpaqueColor): OpaqueTextureAlgorithm = { x, y -> color }

val checkerPattern = { first: OpaqueColor, second: OpaqueColor ->
  { x: Float, y: Float ->
    if (flip(x < 0.5f, y < 0.5f))
      first
    else
      second
  }
}

val noiseSource = OpenSimplexNoiseKotlin(1)

fun tileable2DNoise(source: OpenSimplexNoiseKotlin, x: Float, y: Float, scale: Float): Float {
  // Noise range
  val x1 = 0f
  val x2 = scale
  val y1 = 0f
  val y2 = scale
  val dx = x2 - x1
  val dy = y2 - y1

  // Sample noise at smaller intervals
  val s = x / 1f
  val t = y / 1f

  // Calculate our 4D coordinates
  val nx = x1 + Math.cos((s * 2f * Pi).toDouble()).toFloat() * dx / (2 * Pi)
  val ny = y1 + Math.cos((t * 2f * Pi).toDouble()).toFloat() * dy / (2 * Pi)
  val nz = x1 + Math.sin((s * 2f * Pi).toDouble()).toFloat() * dx / (2 * Pi)
  val nw = y1 + Math.sin((t * 2f * Pi).toDouble()).toFloat() * dy / (2 * Pi)
  return source.eval(nx, ny, nz, nw).toFloat()
}

fun simpleNoise(scale: Float): ScalarTextureAlgorithm =
    { x, y ->
      noiseSource.eval(x * scale, y * scale)
//      tileable2DNoise(noiseSource, x, y, scale)
    }

fun simpleNoise(scales: List<Float>): ScalarTextureAlgorithm =
    { x, y ->
      scales.map { simpleNoise(it)(x, y) / scales.size }
          .sum()
    }

fun colorize(first: OpaqueColor, second: OpaqueColor, mod: Float): Vector3 =
    first * (1 - mod) + second * mod

fun colorize(a: OpaqueColor, b: OpaqueColor, algorithm: ScalarTextureAlgorithm): OpaqueTextureAlgorithm = { x, y ->
  colorize(a, b, algorithm(x, y))
}

fun createTextureBuffer(algorithm: OpaqueTextureAlgorithm, width: Int, height: Int = width): FloatBuffer {
  val buffer = BufferUtils.createFloatBuffer(width * height * 3)
  for (y in 0 until width) {
    for (x in 0 until height) {
      buffer.put(algorithm(x / width.toFloat(), y / height.toFloat()))
    }
  }
  buffer.flip()
  return buffer
}

//fun createTexture(algorithm: OpaqueTextureAlgorithm, attributes: TextureAttributes, width: Int, height: Int = width): Texture {
//  val buffer = createTextureBuffer(algorithm, width, height)
//  return Texture(width, height, buffer, geometryTextureInitializer)
//}