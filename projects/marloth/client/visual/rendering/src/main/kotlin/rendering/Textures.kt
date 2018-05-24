package rendering

import mythic.glowing.Texture
import mythic.spatial.Vector3
import texture_generation.OpaqueTextureAlgorithm
import texture_generation.checkerPattern
import texture_generation.createTexture
import texture_generation.simpleNoise

fun createCheckers(): OpaqueTextureAlgorithm {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  return checkerPattern(black, white)
}

fun createDarkCheckers(): OpaqueTextureAlgorithm {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val red = Vector3(0.55f, 0.3f, 0.0f)
  return checkerPattern(black, red)
}

fun createGrayNoise(): OpaqueTextureAlgorithm {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  return simpleNoise(black, white)
}

enum class Textures {
  checkers,
  darkCheckers,
  grayNoise
}

typealias TextureLibrary = Map<Textures, Texture>

val textureLibrary: Map<Textures, OpaqueTextureAlgorithm> = mapOf(
    Textures.checkers to createCheckers(),
    Textures.darkCheckers to createDarkCheckers(),
    Textures.grayNoise to createGrayNoise()
)

fun createTextureLibrary() =
    textureLibrary.mapValues { createTexture(it.value, 256) }
