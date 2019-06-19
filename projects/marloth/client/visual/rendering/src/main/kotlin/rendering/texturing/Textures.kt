package rendering.texturing

import getResourceUrl
import mythic.ent.pipe
import mythic.glowing.Texture
import mythic.glowing.TextureAttributes
import mythic.glowing.TextureStorageUnit
import mythic.imaging.*
import mythic.platforming.ImageLoader
import mythic.platforming.RawImage
import mythic.spatial.Vector3
import org.joml.Vector2i
import rendering.meshes.loading.loadJsonResource
import rendering.toCamelCase
import scanResources
import scanTextureResources
import scenery.TextureId
import silentorb.metahub.core.Engine
import silentorb.metahub.core.Graph
import silentorb.metahub.core.executeAndFormat
import silentorb.metahub.core.mapValues
import java.nio.FloatBuffer
import java.nio.file.Paths
import kotlin.concurrent.thread

typealias ImageSource = () -> RawImage?

data class DeferredTexture(
    val name: String,
    val attributes: TextureAttributes,
    val load: ImageSource
)

data class LoadedTextureData(
    val name: String,
    val attributes: TextureAttributes,
    val image: RawImage
)

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

typealias TextureLibrary = Map<TextureId, Texture>
typealias DynamicTextureLibrary = MutableMap<String, Texture>
typealias OpaqueTextureAlgorithmSource = () -> OpaqueTextureAlgorithm
typealias TextureGenerator = (scale: Float) -> Texture
typealias TextureGeneratorMap = Map<TextureId, TextureGenerator>

fun basicTextures(): Map<TextureId, OpaqueTextureAlgorithmSource> = mapOf(

    TextureId.checkers to createCheckers(),
//    Textures.darkCheckers to createDarkCheckers(),
    TextureId.debugCyan to { solidColor(Vector3(0f, 1f, 1f)) },
    TextureId.debugMagenta to { solidColor(Vector3(1f, 0f, 1f)) },
    TextureId.debugYellow to { solidColor(Vector3(1f, 1f, 0f)) },

    TextureId.ground to {
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

fun loadTextureFromFile(loadImage: ImageLoader, path: String, attributes: TextureAttributes): Texture {
  val fullPath = getResourceUrl(path).path.substring(1)
  println("Image " + path)
  val image = loadImage(fullPath)!!
  return Texture(image.width, image.height, attributes, image.buffer)
}

fun deferImageFile(loadImage: ImageLoader, path: String, attributes: TextureAttributes): DeferredTexture {
  val fullPath = getResourceUrl(path).path.substring(1)
  println("Image " + path)
  return DeferredTexture(
      name = getFileShortName(path),
      attributes = attributes,
      load = { loadImage(fullPath) }
  )
}


fun loadTextureGraph(engine: Engine, path: String): Graph =
    pipe(loadJsonResource<Graph>(path), listOf(mapValues(engine)))

fun loadProceduralTextureFromFile(engine: Engine, path: String, attributes: TextureAttributes, length: Int): Texture {
  val graph = loadTextureGraph(engine, path)
  val values = executeAndFormat(engine, graph)
  val diffuse = values["diffuse"]!! as FloatBuffer
  return Texture(length, length, attributes, rgbFloatToBytes(diffuse))
}

fun deferProceduralTextureFromFile(engine: Engine, path: String, name: String, attributes: TextureAttributes, length: Int): DeferredTexture {
  val load: ImageSource = {
    val graph = loadTextureGraph(engine, path)
    val values = executeAndFormat(engine, graph)
    val diffuse = values["diffuse"]!! as FloatBuffer
    RawImage(
        buffer = rgbFloatToBytes(diffuse),
        width = length,
        height = length
    )
  }

  return DeferredTexture(
      name = name,
      attributes = attributes,
      load = load
  )
}

private fun miscTextureGenerators(): TextureGeneratorMap = mapOf(
    TextureId.background to applyAlgorithm(colorize(
        Vector3(0.15f),
        Vector3(0.25f),
        simpleNoise(listOf(12f, 37f))
    ), 512, TextureAttributes(repeating = false, storageUnit = TextureStorageUnit.unsigned_byte)),
    TextureId.grass to applyAlgorithm(colorize(
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
  val engine = newTextureEngine(Vector2i(length))

  return listProceduralTextures()
      .filter { it.second != "rayMarch" }
      .associate { (path, name) ->
        val texture = loadProceduralTextureFromFile(engine, path, attributes, length)
        Pair(name, texture)
      }
}

fun deferProceduralTextures(attributes: TextureAttributes): List<DeferredTexture> {
  val length = 256
  val engine = newTextureEngine(Vector2i(length))

  return listProceduralTextures()
      .filter { it.second != "rayMarch" }
      .map { (path, name) ->
        deferProceduralTextureFromFile(engine, path, name, attributes, length)
      }
}

fun gatherTextures(loadImage: ImageLoader, attributes: TextureAttributes): List<DeferredTexture> =
    scanTextureResources("models")
        .plus(scanTextureResources("textures"))
        .map {
          deferImageFile(loadImage, it, attributes)
        }
        .plus(deferProceduralTextures(attributes))

//fun loadTextures(loadImage: ImageLoader, attributes: TextureAttributes): Map<String, Texture> =
//    scanTextureResources("models")
//        .plus(scanTextureResources("textures"))
//        .associate {
//          Pair(
//              getFileShortName(it),
//              loadTextureFromFile(loadImage, it, attributes)
//          )
//        }
//        .plus(loadProceduralTextures(attributes))

//fun loadDeferredTextures2(list: List<DeferredTexture>): Map<String, Texture> {
//  return list.mapNotNull { deferred ->
//    val image = deferred.load()
//    if (image != null)
//      Pair(deferred.name, Texture(image.width, image.height, deferred.attributes, image.buffer))
//    else
//      null
//  }.associate { it }
//}

fun loadDeferredTextures(list: List<DeferredTexture>): List<LoadedTextureData> {
  return list.mapNotNull { deferred ->
    val image = deferred.load()
    if (image != null)
      LoadedTextureData(
          name = deferred.name,
          attributes = deferred.attributes,
          image = image
      )
    else
      null
  }
}

fun texturesToGpu(list: List<LoadedTextureData>): Map<String, Texture> {
  return list.map {
    Pair(it.name, Texture(it.image.width, it.image.height, it.attributes, it.image.buffer))
  }.associate { it }
}

data class AsyncTextureLoader(
    var remainingTextures: List<DeferredTexture>,
    var batchSize: Int = 8,
    var loadedTextureBuffer: List<LoadedTextureData>? = null,
    var isLoading: Boolean = false
)

fun updateAsyncTextureLoading(loader: AsyncTextureLoader, destination: DynamicTextureLibrary) {
  if (loader.isLoading)
    return

  loader.isLoading = true
  val loadedTextures = loader.loadedTextureBuffer
  if (loadedTextures != null) {
    destination += texturesToGpu(loadedTextures)
    loader.loadedTextureBuffer = null
  }
  if (loader.remainingTextures.none())
    return

  val next = loader.remainingTextures.take(loader.batchSize)
  loader.remainingTextures = loader.remainingTextures.drop(loader.batchSize)

  thread(start = true) {
    loader.loadedTextureBuffer = loadDeferredTextures(next)
    loader.isLoading = false
  }
}
