package rendering.texturing

import getResourceUrl
import mythic.imaging.*
import silentorb.metahub.core.Engine
import silentorb.metahub.core.Graph
import silentorb.metahub.core.executeAndFormat
import silentorb.metahub.core.mapValues
import mythic.ent.pipe
import mythic.glowing.Texture
import mythic.glowing.TextureAttributes
import mythic.glowing.TextureStorageUnit
import mythic.glowing.loadImageBuffer
import mythic.quartz.globalProfiler
import mythic.spatial.Vector3
import rendering.meshes.loading.loadJsonResource
import mythic.imaging.newTextureEngine
import rendering.toCamelCase
import scanResources
import scanTextureResources
import scenery.Textures
import java.nio.FloatBuffer
import java.nio.file.Paths

fun mix(first: OpaqueTextureAlgorithm, firstPercentage: Float, second: OpaqueTextureAlgorithm) = { x: Float, y: Float ->
  first(x, y) * firstPercentage + second(x, y) * (1 - firstPercentage)
}

val noisyCheckerPattern = { a: Vector3, b: Vector3 ->
  { x: Float, y: Float ->
    checkerPattern(a, b)(x, y) * 0.7f + grayNoise(listOf(24f, 64f))(x, y) * 0.3f
  }
}

fun createCheckers() = {
  val black = Vector3(0.0f, 0.0f, 0.0f)
//  val white = Vector3(0.0f, 1.0f, 1.0f)
  val white = Vector3(1.0f, 1.0f, 1.0f)
  mix(
      checkerPattern(black, white),
      0.7f,
      grayNoise(listOf(24f, 64f))
  )
}

fun createDarkCheckers() = {
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
typealias DynamicTextureLibrary = Map<String, Texture>
typealias OpaqueTextureAlgorithmSource = () -> OpaqueTextureAlgorithm
typealias TextureGenerator = (scale: Float) -> Texture
typealias TextureGeneratorMap = Map<Textures, TextureGenerator>

fun basicTextures(): Map<Textures, OpaqueTextureAlgorithmSource> = mapOf(

    Textures.checkers to createCheckers(),
//    Textures.darkCheckers to createDarkCheckers(),
    Textures.debugCyan to { solidColor(Vector3(0f, 1f, 1f)) },
    Textures.debugMagenta to { solidColor(Vector3(1f, 0f, 1f)) },
    Textures.debugYellow to { solidColor(Vector3(1f, 1f, 0f)) },

    Textures.ground to {
      colorize(
          Vector3(0.0f, 0.0f, 0.0f),
          Vector3(0.55f, 0.35f, 0.0f),
          simpleNoise(listOf(24f, 64f))
      )
    }
)

fun applyAlgorithm(algorithm: OpaqueTextureAlgorithm, length: Int, attributes: TextureAttributes): TextureGenerator = { scale ->
  val scaledLength = (length * scale).toInt()
  val buffer = createTextureBuffer(algorithm, scaledLength, scaledLength)
  Texture(scaledLength, scaledLength, attributes, buffer)
}

fun loadTextureFromFile(path: String, attributes: TextureAttributes): Texture {
  val (buffer, dimensions) = loadImageBuffer(getResourceUrl(path))
  return Texture(dimensions.x, dimensions.y, attributes, buffer)
}

fun loadTextureGraph(engine: Engine, path: String): Graph =
    pipe(loadJsonResource<Graph>(path), listOf(mapValues(engine)))

fun loadProceduralTextureFromFile(engine: Engine, path: String, attributes: TextureAttributes, length: Int): Texture {
  val graph = loadTextureGraph(engine, path)
  val values = executeAndFormat(engine, graph)
  val diffuse = values["diffuse"]!! as FloatBuffer
  return Texture(length, length, attributes, floatTextureToBytes(diffuse))
}

private fun miscTextureGenerators(): TextureGeneratorMap = mapOf(
    Textures.background to applyAlgorithm(colorize(
        Vector3(0.15f),
        Vector3(0.25f),
        simpleNoise(listOf(12f, 37f))
    ), 512, TextureAttributes(repeating = false, storageUnit = TextureStorageUnit.unsigned_byte)),
    Textures.grass to applyAlgorithm(colorize(
        Vector3(0.25f, 0.35f, 0.05f),
        Vector3(0.5f, 0.65f, 0.2f),
        simpleNoise(listOf(62f, 37f))
    ), 512, TextureAttributes(repeating = true, mipmap = true, storageUnit = TextureStorageUnit.unsigned_byte))
)

private fun basicTextureGenerators(): TextureGeneratorMap = basicTextures().mapValues { algorithm ->
  applyAlgorithm(algorithm.value(), 256, TextureAttributes(repeating = true, mipmap = true, storageUnit = TextureStorageUnit.unsigned_byte))
}

fun textureGenerators(): TextureGeneratorMap =
    basicTextureGenerators()
        .plus(miscTextureGenerators())

fun createTextureLibrary(scale: Float) =
    textureGenerators().mapValues { it.value(scale) }

fun getFileShortName(path: String): String =
    toCamelCase(Paths.get(path).fileName.toString().substringBeforeLast("."))

fun listProceduralTextures(): List<Pair<String, String>> =
    scanResources("procedural/textures", listOf(".json"))
        .map { Pair(it, getFileShortName(it)) }

fun loadProceduralTextures(attributes: TextureAttributes): Map<String, Texture> {
  val length = 256
  val engine = newTextureEngine(length)
  return listProceduralTextures()
      .associate { (path, name) ->
        val texture = loadProceduralTextureFromFile(engine, path, attributes, length)
        Pair(name, texture)
      }
}

fun loadTextures(attributes: TextureAttributes): Map<String, Texture> =
    globalProfiler().wrap("textures") {
      scanTextureResources("models")
          .plus(scanTextureResources("textures"))
          .associate {
            Pair(
                getFileShortName(it),
                loadTextureFromFile(it, attributes)
            )
          }
          .plus(loadProceduralTextures(attributes))
    }
