package rendering

import mythic.glowing.UniformBuffer
import mythic.spatial.putVector3
import mythic.spatial.putVector4
import org.lwjgl.BufferUtils
import scenery.Light
import java.nio.ByteBuffer

fun padBuffer(buffer: ByteBuffer, count: Int) {
  for (i in 0 until count) {
    buffer.putFloat(0f)
  }
}

fun createLightBuffer(lights: List<Light>): ByteBuffer {
  val headerSize = 4
//  val lightSize = 12
  val lightSize = 16
  val totalSize = headerSize + lightSize * lights.size

  val buffer = BufferUtils.createByteBuffer(totalSize * 4)
  buffer.putInt(2)
  padBuffer(buffer, 3)
  for (light in lights) {
    buffer.putInt(light.type.value)
    padBuffer(buffer, 3)
    buffer.putVector4(light.color)
    buffer.putVector3(light.position)
    padBuffer(buffer, 1)
    buffer.putVector3(light.direction)
    padBuffer(buffer, 1)
  }
  buffer.flip()
  return buffer
}

fun updateLights(lights: List<Light>, uniformBuffer: UniformBuffer) {
  val byteBuffer = createLightBuffer(lights)
  uniformBuffer.load(byteBuffer)
}