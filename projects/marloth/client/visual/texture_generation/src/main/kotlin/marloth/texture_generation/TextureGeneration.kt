package texture_generation

import marloth.texture_generation.OpenSimplexNoise
import mythic.glowing.Texture
import mythic.glowing.geometryTextureInitializer
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

val checkerPattern = { first: OpaqueColor, second: OpaqueColor ->
  { x: Float, y: Float ->
    if (flip(x < 0.5f, y < 0.5f))
      first
    else
      second
  }
}

val noiseSource = OpenSimplexNoise(1)

fun simpleNoise(scale: Float): ScalarTextureAlgorithm =
    { x, y ->
      noiseSource.eval(x * scale.toDouble(), y * scale.toDouble()).toFloat()
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

fun createTexture(algorithm: OpaqueTextureAlgorithm, width: Int, height: Int = width): Texture {
  val buffer = createTextureBuffer(algorithm, width, height)
  return Texture(width, height, buffer, geometryTextureInitializer)
}