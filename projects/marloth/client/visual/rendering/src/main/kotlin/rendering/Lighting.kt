package rendering

import mythic.spatial.Vector3
import mythic.spatial.Vector4
import mythic.spatial.putVector3
import mythic.spatial.putVector4
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

enum class LightType(val value: Int) {
  point(1),
  spot(2)
}

data class Light(
    var type: LightType,
    var color: Vector4, // Includes brightness
    var position: Vector3,
    var direction: Vector3
)

fun padBuffer(buffer: ByteBuffer, count: Int) {
  for (i in 0 until count) {
    buffer.putFloat(0f)
  }
}

fun createLightBuffer(lights: List<Light>): ByteBuffer {
  val headerSize = 4
  val lightSize = 12
  val totalSize = headerSize + lightSize * lights.size

  val buffer = BufferUtils.createByteBuffer(totalSize)
  buffer.putInt(lights.size)
  padBuffer(buffer, 3)
  for (light in lights) {
    buffer.putInt(light.type.value)
    buffer.putVector4(light.color)
    buffer.putVector3(light.position)
    buffer.putVector3(light.direction)
    padBuffer(buffer, 1)
  }
  return buffer
}