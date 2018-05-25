package rendering

import mythic.glowing.Texture
import mythic.spatial.Vector3
import texture_generation.*

val createCheckers = {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  checkerPattern(black, white)
}

val createDarkCheckers = {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val red = Vector3(0.55f, 0.3f, 0.0f)
  checkerPattern(black, red)
}

val createGrayNoise = {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  colorize(black, white, simpleNoise(listOf(24f, 64f)))
}

enum class Textures {
  checkers,
  darkCheckers,
  grayNoise
}

typealias TextureLibrary = Map<Textures, Texture>
typealias OpaqueTextureAlgorithmSource = () -> OpaqueTextureAlgorithm

val textureLibrary: Map<Textures, OpaqueTextureAlgorithmSource> = mapOf(

    Textures.checkers to createCheckers,

    Textures.darkCheckers to createDarkCheckers,

    Textures.grayNoise to {
      colorize(
          Vector3(0.0f, 0.0f, 0.0f),
          Vector3(0.55f, 0.35f, 0.0f),
          simpleNoise(listOf(24f, 64f))
      )
    }
)

fun createTextureLibrary() =
    textureLibrary.mapValues { createTexture(it.value(), 256) }
