package texture_generation

import marloth.texture_generation.OpenSimplexNoise
import mythic.glowing.Texture
import mythic.glowing.geometryTextureInitializer
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.put
import mythic.spatial.times
import org.joml.plus
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

typealias OpaqueColor = mythic.spatial.Vector3
typealias TransparentColor = mythic.spatial.Vector4

typealias TextureAlgorithm<T> = (x: Int, y: Int, width: Int, height: Int) -> T

typealias OpaqueTextureAlgorithm = TextureAlgorithm<OpaqueColor>
typealias TransparentTextureAlgorithm = TextureAlgorithm<TransparentColor>

fun flip(a: Boolean, b: Boolean) = if (b) a else !a

val checkerPattern = { first: OpaqueColor, second: OpaqueColor ->
  { x: Int, y: Int, width: Int, height: Int ->
    if (flip(x < width / 2, y < height / 2))
      first
    else
      second
  }
}

fun simpleNoise(first: OpaqueColor, second: OpaqueColor): OpaqueTextureAlgorithm {
  val generator = OpenSimplexNoise(1)
  return { x: Int, y: Int, width: Int, height: Int ->
    val scale = 24f
    val mod = generator.eval((x.toDouble() / width.toDouble()) * scale, (y.toDouble() / width.toDouble()) * scale).toFloat()
    //first * mod + second * (1 - mod)
    Vector3(mod, mod, mod)
  }
}

fun createTextureBuffer(algorithm: OpaqueTextureAlgorithm, width: Int, height: Int = width): FloatBuffer {
  val buffer = BufferUtils.createFloatBuffer(width * height * 3)
  for (y in 0 until width) {
    for (x in 0 until height) {
      buffer.put(algorithm(x, y, width, height))
    }
  }
  buffer.flip()
  return buffer
}

fun createTexture(algorithm: OpaqueTextureAlgorithm, width: Int, height: Int = width): Texture {
  val buffer = createTextureBuffer(algorithm, width, height)
  return Texture(width, height, buffer, geometryTextureInitializer)
}