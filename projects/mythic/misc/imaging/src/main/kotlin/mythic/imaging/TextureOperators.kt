package mythic.imaging

import java.nio.ByteBuffer
import metahub.Function
import metahub.Arguments
import metahub.Engine
import metahub.TypeMapper
import mythic.spatial.Vector3
import org.joml.Vector3i
import org.lwjgl.BufferUtils
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

fun <T> withBuffer(depth: Int, setter: (FloatBuffer, T) -> Unit): ((Arguments) -> (Float, Float) -> T) -> TextureFunction = { function ->
  { length ->
    { arguments ->
      val buffer = BufferUtils.createFloatBuffer(length * length * depth)
      val getter = function(arguments)
      for (y in 0 until length) {
        for (x in 0 until length) {
          val value = getter(x.toFloat() / length, y.toFloat() / length)
          setter(buffer, value)
        }
      }
      buffer.rewind()
      buffer
    }
  }
}

val withBitmapBuffer = withBuffer<Vector3>(3) { buffer, value ->
  buffer.put(value)
}

val withGrayscaleBuffer = withBuffer<Float>(1) { buffer, value ->
  buffer.put(value)
}

fun convertColor(value: Vector3): Vector3i =
    Vector3i(
        (value.x * 255).toInt(),
        (value.y * 255).toInt(),
        (value.z * 255).toInt()
    )

val solidColor: TextureFunction = withBitmapBuffer { arguments ->
  val color = arguments["color"]!! as SolidColor
  { _, _ -> color }
}

val coloredCheckers: TextureFunction = withBitmapBuffer { arguments ->
  val first = arguments["firstColor"]!! as SolidColor
  val second = arguments["secondColor"]!! as SolidColor
  checkerPattern(first, second)
}

val grayscaleCheckers: TextureFunction = withGrayscaleBuffer { arguments ->
  checkerOp(0f, 1f)
}

val colorize: TextureFunction = withBitmapBuffer { arguments ->
  val grayscale = arguments["grayscale"]!! as FloatBuffer
  grayscale.rewind()
  val first = arguments["firstColor"]!! as SolidColor
  val second = arguments["secondColor"]!! as SolidColor
  { x, y ->
    val unit = grayscale.get()
    first * (1f - unit) + second * unit
  }
}

private val textureFunctions = mapOf(
    "coloredCheckers" to coloredCheckers,
    "checkers" to grayscaleCheckers,
    "colorize" to colorize,
    "solidColor" to solidColor
)

private val typeMappers: List<TypeMapper> = listOf(
    { value ->
      if (value is List<*>) {
        val list = value as ArrayList<Float>
        Vector3(list[0], list[1], list[2])
      } else if (value is Map<*, *>) {
        val map = value as Map<String, Double>
        Vector3(value["x"]!!.toFloat(), value["y"]!!.toFloat(), value["z"]!!.toFloat())
      } else
        null
    }
)

fun newTextureEngine(length: Int): Engine =
    Engine(
        functions = textureFunctions.mapValues { it.value(length) },
        typeMappers = typeMappers
    )