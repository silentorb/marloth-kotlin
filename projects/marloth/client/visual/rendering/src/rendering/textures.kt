package rendering

import marloth.texture_generation.checkerPattern
import marloth.texture_generation.createTexture
import mythic.glowing.Texture
import mythic.spatial.Vector3

fun createCheckers(): Texture {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  return createTexture(checkerPattern(black, white), 256)
}

fun createDarkCheckers(): Texture {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val red = Vector3(1.0f, 0.0f, 0.0f)
  return createTexture(checkerPattern(black, red), 256)
}

data class Textures(
    val checkers: Texture = createCheckers(),
    val darkCheckers: Texture = createDarkCheckers()
)