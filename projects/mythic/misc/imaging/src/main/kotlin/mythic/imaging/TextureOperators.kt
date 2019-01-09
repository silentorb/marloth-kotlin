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

fun withNewBuffer(function: (Int, FloatBuffer, Arguments) -> Any): TextureFunction = { length ->
  { arguments ->
    val buffer = allocateFloatTextureBuffer(length)
    function(length, buffer, arguments)
    buffer.rewind()
    buffer
  }
}

fun convertColor(value: Vector3): Vector3i =
    Vector3i(
        (value.x * 255).toInt(),
        (value.y * 255).toInt(),
        (value.z * 255).toInt()
    )

val solidColor: TextureFunction = withNewBuffer { length, buffer, arguments ->
  val color = arguments["color"]!! as SolidColor
  for (i in 1..length * length) {
    buffer.put(color)
  }
  buffer
}

val checkers: TextureFunction = withNewBuffer { length, buffer, arguments ->
  val first = arguments["firstColor"]!! as SolidColor
  val second = arguments["secondColor"]!! as SolidColor
  val pattern = checkerPattern(first, second)

  for (y in 0 until length) {
    for (x in 0 until length) {
      val color = pattern(x.toFloat() / length, y.toFloat() / length)
      buffer.put(color)
    }
  }
}

private val textureFunctions = mapOf(
    "checkers" to checkers,
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