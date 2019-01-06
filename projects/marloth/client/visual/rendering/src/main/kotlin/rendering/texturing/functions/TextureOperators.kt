package rendering.texturing.functions

import marloth.texture_generation.checkerPattern2
import java.nio.ByteBuffer
import metahub.Function
import metahub.Arguments
import metahub.Engine
import metahub.TypeMapper
import mythic.spatial.Vector3
import org.joml.Vector3i
import org.lwjgl.BufferUtils

typealias SolidColor = Vector3

typealias TextureFunction = (Int) -> Function

fun allocateBuffer(length: Int): ByteBuffer =
    BufferUtils.createByteBuffer(length * length * 3)

fun ByteBuffer.put(color: Vector3i) {
  this.put(color.x.toByte())
  this.put(color.y.toByte())
  this.put(color.z.toByte())
}

fun withNewBuffer(function: (Int, ByteBuffer, Arguments) -> Any): TextureFunction = { length ->
  { arguments ->
    val buffer = allocateBuffer(length)
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
  val color = convertColor(arguments["color"]!! as SolidColor)
  for (i in 1..length * length) {
    buffer.put(color)
  }
  buffer
}

val checkers: TextureFunction = withNewBuffer { length, buffer, arguments ->
  val first = convertColor(arguments["firstColor"]!! as SolidColor)
  val second = convertColor(arguments["secondColor"]!! as SolidColor)
  val pattern = checkerPattern2(first, second)

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
      } else
        null
    }
)

fun newTextureEngine(length: Int): Engine =
    Engine(
        functions = textureFunctions.mapValues { it.value(length) },
        typeMappers = typeMappers
    )