package mythic.imaging

import silentorb.metahub.core.Arguments
import silentorb.metahub.core.Engine
import silentorb.metahub.core.Function
import silentorb.metahub.core.TypeMapper
import mythic.ent.cached
import mythic.spatial.Vector3
import org.joml.Vector3i
import org.lwjgl.BufferUtils
import randomly.Dice
import silentorb.raymarching.*
import java.nio.ByteBuffer
import java.nio.FloatBuffer

typealias SolidColor = Vector3

typealias TextureFunction = (Int) -> Function

fun allocateFloatTextureBuffer(length: Int): FloatBuffer =
    BufferUtils.createFloatBuffer(length * length * 3)

fun allocateByteTextureBuffer(length: Int): ByteBuffer =
    BufferUtils.createByteBuffer(length * length * 3)

fun floatTextureToBytes(buffer: FloatBuffer): ByteBuffer {
  val byteBuffer = BufferUtils.createByteBuffer(buffer.capacity())
  buffer.rewind()
  (1..buffer.capacity()).forEach {
    val value = buffer.get()
    byteBuffer.put((value * 255).toByte())
  }
  byteBuffer.rewind()
  return byteBuffer
}

fun grayscaleTextureToBytes(buffer: FloatBuffer): ByteBuffer {
  val byteBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 3)
  buffer.rewind()
  (1..buffer.capacity()).forEach {
    val value = (buffer.get() * 255).toByte()
    byteBuffer.put(value)
    byteBuffer.put(value)
    byteBuffer.put(value)
  }
  byteBuffer.rewind()
  return byteBuffer
}

fun ByteBuffer.put(color: Vector3i) {
  this.put(color.x.toByte())
  this.put(color.y.toByte())
  this.put(color.z.toByte())
}

fun FloatBuffer.put(color: Vector3) {
  this.put(color.x)
  this.put(color.y)
  this.put(color.z)
}

fun FloatBuffer.getVector3() =
    Vector3(
        this.get(),
        this.get(),
        this.get()
    )

fun fillBuffer(depth: Int, length: Int, action: (FloatBuffer) -> Unit): FloatBuffer {
  val buffer = BufferUtils.createFloatBuffer(length * length * depth)
  action(buffer)
  buffer.rewind()
  return buffer
}

data class BufferInfo<T>(
    val depth: Int,
    val setter: (FloatBuffer, T) -> Unit
)

fun <T> withBuffer(bufferInfo: BufferInfo<T>, function: (Arguments) -> (Float, Float) -> T): TextureFunction =
    { length ->
      { arguments ->
        fillBuffer(bufferInfo.depth, length) { buffer ->
          val getter = function(arguments)
          for (y in 0 until length) {
            for (x in 0 until length) {
              val value = getter(x.toFloat() / length, 1f - y.toFloat() / length)
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

val solidColor: TextureFunction = withBuffer(withBitmapBuffer) { arguments ->
  val color = arguments["color"]!! as SolidColor
  { _, _ -> color }
}

val coloredCheckers: TextureFunction = withBuffer(withBitmapBuffer) { arguments ->
  val first = arguments["firstColor"]!! as SolidColor
  val second = arguments["secondColor"]!! as SolidColor
  checkerPattern(first, second)
}

val grayscaleCheckers: TextureFunction = withBuffer(withGrayscaleBuffer) { arguments ->
  checkerOp(0f, 1f)
}

val colorize: TextureFunction = withBuffer(withBitmapBuffer) { arguments ->
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

val maskOperator: TextureFunction = withBuffer(withBitmapBuffer) { arguments ->
  val first = floatBufferArgument(arguments, "first")
  val second = floatBufferArgument(arguments, "second")
  val mask = floatBufferArgument(arguments, "mask")
  val k = 0
  { x, y ->
    val degree = mask.get()
    first.getVector3() * (1f - degree) + second.getVector3() * degree
  }
}

val mixBitmaps: TextureFunction = withBuffer(withBitmapBuffer) { arguments ->
  val degree = arguments["degree"]!! as Float
  val first = floatBufferArgument(arguments, "first")
  val second = floatBufferArgument(arguments, "second")
  val k = 0
  { x, y ->
    first.getVector3() * (1f - degree) + second.getVector3() * degree
  }
}

val mixGrayscales: TextureFunction = withBuffer(withGrayscaleBuffer) { arguments ->
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

val simpleNoiseOperator: TextureFunction = withBuffer(withGrayscaleBuffer) { arguments ->
  val offset = arguments["offset"]!! as Int
  val periods = arguments["periods"]!! as Int
  val grid = dotGridGradient(periods, offset)
  val k = 0
  { x, y ->
    perlin2d(grid, x * periods.toFloat(), y * periods.toFloat())

  }
}

val voronoiBoundaryOperator: TextureFunction = withBuffer(withGrayscaleBuffer) { arguments ->
  val dice = Dice(1)
  val length = 10
  val grid = newAnchorGrid(dice, length, 10)
  val nearestCells = cached(getNearestCells(grid, 2))
  voronoi(length, nearestCells, voronoiBoundaries(0.05f * grid.length.toFloat()))
//  { x, y ->
//0f
//  }
}

data class MarchedBuffers(
    val color: FloatBuffer,
    val depth: FloatBuffer,
    val position: FloatBuffer,
    val normal: FloatBuffer
)

val rayMarchOperator: TextureFunction =
    { length ->
      { arguments ->
        val marcher = Marcher(
            end = 5f,
            maxSteps = 100
        )

        val scene = Scene(
            camera = Camera(
                position = Vector3(),
                direction = Vector3(0f, 1f, 0f).normalize()
            ),
            sdf = sampleScene(),
            lights = listOf()
        )
        val area = length * length
        val buffers = MarchedBuffers(
            color = BufferUtils.createFloatBuffer(area * 3),
            depth = BufferUtils.createFloatBuffer(area),
            position = BufferUtils.createFloatBuffer(area * 3),
            normal = BufferUtils.createFloatBuffer(area * 3)
        )

        val render = pixelRenderer(marcher, scene)

        for (y in 0 until length) {
          for (x in 0 until length) {
            val point = render(x.toFloat() / length, 1f - y.toFloat() / length)
            buffers.color.put(point.color)
            buffers.depth.put(point.depth)
            buffers.position.put(point.position)
            buffers.normal.put(point.normal)
          }
        }

        buffers.color.rewind()
        buffers.depth.rewind()
        buffers.position.rewind()
        buffers.normal.rewind()

        mapOf(
            "color" to buffers.color,
            "depth" to buffers.depth,
            "position" to buffers.position,
            "normal" to buffers.normal
        )
      }
    }

val illuminationOperator: TextureFunction = withBuffer(withGrayscaleBuffer) { arguments ->
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
    { length ->
      { arguments ->
        val color = floatBufferArgument(arguments, "color")
        val illumination = floatBufferArgument(arguments, "illumination")
        val k = 0
        fillBuffer(3, length) { buffer ->
          for (y in 0 until length) {
            for (x in 0 until length) {
              buffer.put(mixColorAndLuminance(color.getVector3(), illumination.get()))
            }
          }
        }
      }
    }

val toneMapping: TextureFunction =
    { length ->
      { arguments ->
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

fun newTextureEngine(length: Int): Engine =
    Engine(
        functions = textureFunctions.mapValues { it.value(length) },
        typeMappers = typeMappers,
        outputTypes = textureOutputTypes
    )