package marloth.texture_generation

import mythic.glowing.Texture
import mythic.glowing.geometryTextureInitializer
import mythic.spatial.put
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