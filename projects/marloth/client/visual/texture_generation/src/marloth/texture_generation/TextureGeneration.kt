package marloth.texture_generation

import mythic.spatial.put
import org.lwjgl.BufferUtils
import java.nio.FloatBuffer

typealias OpaqueColor = mythic.spatial.Vector3
typealias TransparentColor = mythic.spatial.Vector4

typealias TextureAlgorithm<T> = (x: Int, y: Int, width: Int, height: Int) -> T

typealias OpaqueTextureAlgorithm = TextureAlgorithm<OpaqueColor>
typealias TransparentTextureAlgorithm = TextureAlgorithm<TransparentColor>

val checkerPattern = { first: OpaqueColor, second: OpaqueColor ->
  { x: Int, y: Int, width: Int, height: Int ->
    if (x < width / 2)
      first
    else
      second
  }
}

fun createTexture(algorithm: OpaqueTextureAlgorithm, width: Int, height: Int = width): FloatBuffer {
  val buffer = BufferUtils.createFloatBuffer(width * height)
  for (y in 0..width) {
    for (x in 0..height) {
      buffer.put(algorithm(x, y, width, height))
    }
  }
  buffer.flip()
  return buffer
}