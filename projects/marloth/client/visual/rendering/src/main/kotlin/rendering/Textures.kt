package rendering

import mythic.glowing.Texture
import mythic.spatial.Vector3
import mythic.spatial.times
import org.joml.plus
import scenery.Textures
import texture_generation.*

fun mix(first: OpaqueTextureAlgorithm, firstPercentage: Float, second: OpaqueTextureAlgorithm) = { x: Float, y: Float ->
  first(x, y) * firstPercentage + second(x, y) * (1 - firstPercentage)
}

val noisyCheckerPattern = { a: Vector3, b: Vector3 ->
  { x: Float, y: Float ->
    checkerPattern(a, b)(x, y) * 0.7f + grayNoise(listOf(24f, 64f))(x, y) * 0.3f
  }
}

val createCheckers = {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  mix(
      checkerPattern(black, white),
      0.7f,
      grayNoise(listOf(24f, 64f))
  )
}

val createDarkCheckers = {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val red = Vector3(0.5f, 0.3f, 0.1f)
  mix(
      checkerPattern(black, red),
      0.9f,
      grayNoise(listOf(12f, 124f))
  )
}

fun grayNoise(scales: List<Float>): OpaqueTextureAlgorithm {
  val black = Vector3(0.0f, 0.0f, 0.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  return colorize(black, white, simpleNoise(scales))
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
