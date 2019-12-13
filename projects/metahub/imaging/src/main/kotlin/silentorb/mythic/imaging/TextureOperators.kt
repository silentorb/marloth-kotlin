package silentorb.mythic.imaging

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.mappedCache
import silentorb.mythic.ent.singleCache
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import org.joml.Vector2i
import org.joml.Vector3i
import org.lwjgl.BufferUtils
import silentorb.mythic.randomly.Dice
import silentorb.metahub.core.Arguments
import silentorb.metahub.core.Engine
import silentorb.metahub.core.Function
import silentorb.metahub.core.TypeMapper
import silentorb.raymarching.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer

typealias SolidColor = Vector3

typealias TextureFunction = (Vector2i) -> Function

fun allocateFloatTextureBuffer(length: Int): FloatBuffer =
    BufferUtils.createFloatBuffer(length * length * 3)

fun allocateByteTextureBuffer(length: Int): ByteBuffer =
    BufferUtils.createByteBuffer(length * length * 3)

fun allocateFloatBuffer(size: Int): FloatBuffer =
    BufferUtils.createFloatBuffer(size)

val bufferCache = { id: Id, size: Int ->
  mappedCache<Id, FloatBuffer> { id2 -> singleCache(::allocateFloatBuffer)(size) }(id)
}

fun fillBuffer(id: Id, depth: Int, dimensions: Vector2i, action: (FloatBuffer) -> Unit): FloatBuffer {
//  val buffer = BufferUtils.createFloatBuffer(dimensions.x * dimensions.y * depth)
  val buffer = bufferCache(id, dimensions.x * dimensions.y * depth)
  action(buffer)
  buffer.rewind()
  return buffer
}

data class BufferInfo<T>(
    val depth: Int,
    val setter: (FloatBuffer, T) -> Unit
)

fun <T> withBuffer(bufferInfo: BufferInfo<T>, function: (Id, Arguments) -> (Float, Float) -> T): TextureFunction =
    { dimensions ->
      { id, arguments ->
        fillBuffer(id, bufferInfo.depth, dimensions) { buffer ->
          val getter = function(id, arguments)
          for (y in 0 until dimensions.y) {
            for (x in 0 until dimensions.x) {
              val value = getter(x.toFloat() / dimensions.x, 1f - y.toFloat() / dimensions.y)
              bufferInfo.setter(buffer, value)
            }
          }
        }
      }
    }

val withBitmapBuffer = BufferInfo<Vector3>(3) { buffer, value ->
  buffer.put(value)
}

val withGrayscaleBuffer = BufferInfo<Float>(1) { buffer, value ->
  buffer.put(value)
}

//val withGrayscaleBuffer = fillBuffer<Float>(1) { buffer, value ->
//  buffer.put(value)
//}

fun convertColor(value: Vector3): Vector3i =
    Vector3i(
        (value.x * 255).toInt(),
        (value.y * 255).toInt(),
        (value.z * 255).toInt()
    )

val solidColor: TextureFunction = withBuffer(withBitmapBuffer) { id, arguments ->
  val color = arguments["color"]!! as SolidColor
  { _, _ -> color }
}

val coloredCheckers: TextureFunction = withBuffer(withBitmapBuffer) { id, arguments ->
  val first = arguments["firstColor"]!! as SolidColor
  val second = arguments["secondColor"]!! as SolidColor
  checkerPattern(first, second)
}

val grayscaleCheckers: TextureFunction = withBuffer(withGrayscaleBuffer) { id, arguments ->
  checkerOp(0f, 1f)
}

val colorize: TextureFunction = withBuffer(withBitmapBuffer) { id, arguments ->
  val grayscale = arguments["grayscale"]!! as FloatBuffer
  grayscale.rewind()
  val first = arguments["firstColor"]!! as SolidColor
  val second = arguments["secondColor"]!! as SolidColor
  { x, y ->
    val unit = grayscale.get()
    first * (1f - unit) + second * unit
  }
}

fun floatBufferArgument(arguments: Arguments, name: String): FloatBuffer {
  val result = arguments[name]!! as FloatBuffer
  result.rewind()
  return result
}

val maskOperator: TextureFunction = withBuffer(withBitmapBuffer) { id, arguments ->
  val first = floatBufferArgument(arguments, "first")
  val second = floatBufferArgument(arguments, "second")
  val mask = floatBufferArgument(arguments, "mask")
  val k = 0
  { x, y ->
    val degree = mask.get()
    first.getVector3() * (1f - degree) + second.getVector3() * degree
  }
}

val mixBitmaps: TextureFunction = withBuffer(withBitmapBuffer) { id, arguments ->
  val degree = arguments["degree"]!! as Float
  val first = floatBufferArgument(arguments, "first")
  val second = floatBufferArgument(arguments, "second")
  val k = 0
  { x, y ->
    first.getVector3() * (1f - degree) + second.getVector3() * degree
  }
}

val mixGrayscales: TextureFunction = withBuffer(withGrayscaleBuffer) { id, arguments ->
  val weights = arguments["weights"]!! as List<Float>
  val buffers = weights.mapIndexed { index, value ->
    Pair(floatBufferArgument(arguments, (index + 1).toString()), value)
  }
//  val first = floatBufferArgument(arguments, "first")
//  val second = floatBufferArgument(arguments, "second")
  val k = 0
  { x, y ->
    buffers.fold(0f) { a, b -> a + b.first.get() * b.second }
//    first.get() * (1f - degrees) + second.get() * degrees
  }
}

val noiseSource = OpenSimplexNoiseKotlin(1)

fun simpleNoise(scale: Float): ScalarTextureAlgorithm =
    { x, y ->
      noiseSource.eval(x * scale, y * scale)
    }

val simpleNoiseOperator: TextureFunction = withBuffer(withGrayscaleBuffer) { id, arguments ->
  val offset = arguments["offset"]!! as Int
  val periods = arguments["periods"]!! as Int
  val grid = dotGridGradient(periods, offset)
  val k = 0
  { x, y ->
    perlin2d(grid, x * periods.toFloat(), y * periods.toFloat())

  }
}

val voronoiBoundaryOperator: TextureFunction = withBuffer(withGrayscaleBuffer) { id, arguments ->
  val dice = Dice(1)
  val length = 10
  val grid = newAnchorGrid(dice, length, 10)
  val nearestCells = mappedCache(getNearestCells(grid, 2))
  voronoi(length, nearestCells, voronoiBoundaries(0.05f * grid.length.toFloat()))
//  { x, y ->
//0f
//  }
}

val rayMarchOperator: TextureFunction =
    { dimensions ->
      { id, arguments ->
        val marcher = Marcher(
            end = 50f,
            maxSteps = 100
        )

        val scene = Scene(
            camera = Camera(
                position = Vector3(-5f, 0f, 0f),
                orientation = Quaternion(),
                near = 0.01f,
                far = 100f
            ),
            sdf = { sampleScene2() },
            lights = listOf()
        )

        val buffers = newMarchBuffers(dimensions.x * dimensions.y)

//        val cast = orthogonalRay(scene.camera, 1f)
        val cast = perspectiveRay(scene.camera)
        renderToMarchBuffers(buffers, marcher, scene, cast, dimensions)

        mapOf(
            "color" to buffers.color,
            "depth" to buffers.depth,
            "position" to buffers.position,
            "normal" to buffers.normal
        )
      }
    }

val illuminationOperator: TextureFunction = withBuffer(withGrayscaleBuffer) { id, arguments ->
  val depth = floatBufferArgument(arguments, "depth")
  val position = floatBufferArgument(arguments, "position")
  val normal = floatBufferArgument(arguments, "normal")
  val lights = listOf(
      Light(position = Vector3(3f, -3f, 3f), brightness = 1f),
      Light(position = Vector3(-3f, 0f, 2f), brightness = 0.8f)
  )
  val k = 0
  { x, y ->
    illuminatePoint(lights, depth.get(), position.getVector3(), normal.getVector3())
  }
}

val mixSceneOperator: TextureFunction =
    { dimensions ->
      { id, arguments ->
        val color = floatBufferArgument(arguments, "color")
        val illumination = floatBufferArgument(arguments, "illumination")
        val k = 0
        fillBuffer(id, 3, dimensions) { buffer ->
          for (y in 0 until dimensions.y) {
            for (x in 0 until dimensions.x) {
              buffer.put(mixColorAndLuminance(color.getVector3(), illumination.get()))
            }
          }
        }
      }
    }

val toneMapping: TextureFunction =
    { length ->
      { id, arguments ->
        val input = arguments["input"]!! as FloatBuffer
        compressRange(input)
      }
    }

private val textureFunctions = mapOf(
    "coloredCheckers" to coloredCheckers,
    "checkers" to grayscaleCheckers,
    "colorize" to colorize,
    "toneMap" to toneMapping,
    "solidColor" to solidColor,
    "mask" to maskOperator,
    "mixBitmaps" to mixBitmaps,
    "mixGrayscales" to mixGrayscales,
    "mixScene" to mixSceneOperator,
    "illumination" to illuminationOperator,
    "perlinNoise" to simpleNoiseOperator,
    "rayMarch" to rayMarchOperator,
    "voronoiBoundaries" to voronoiBoundaryOperator
)

val textureOutputTypes = setOf("textureOutput")

private val typeMappers: List<TypeMapper> = listOf(
    { value ->
      if (value is List<*>) {
        val list = value as ArrayList<Float>
        list.toList()
      } else if (value is Map<*, *>) {
        val map = value as Map<String, Double>
        Vector3(map["x"]!!.toFloat(), map["y"]!!.toFloat(), map["z"]!!.toFloat())
      } else if (value is Double)
        value.toFloat()
      else
        null
    }
)

fun newTextureEngine(dimensions: Vector2i): Engine =
    Engine(
        functions = textureFunctions.mapValues { it.value(dimensions) },
        typeMappers = typeMappers,
        outputTypes = textureOutputTypes
    )
