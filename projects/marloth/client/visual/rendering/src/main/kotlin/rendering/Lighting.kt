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

private const val headerSize = 4 * 4
private const val lightSize = 16 * 4
private const val maxLights = 20
const val sceneBufferSize = headerSize + lightSize * maxLights

fun createLightBuffer(lights: List<Light>): ByteBuffer {
//  val lightSize = 12

  val totalSize = headerSize + lightSize * lights.size

  val buffer = BufferUtils.createByteBuffer(totalSize)
  buffer.putInt(lights.size)
  padBuffer(buffer, 3)
  for (light in lights) {
    buffer.putInt(light.type.value)
    padBuffer(buffer, 3)
    buffer.putVector4(light.color)
    buffer.putVector3(light.position)
    padBuffer(buffer, 1)
    buffer.putVector4(light.direction)
  }
  buffer.flip()
  return buffer
}

fun updateLights(lights: List<Light>, uniformBuffer: UniformBuffer) {
  val byteBuffer = createLightBuffer(lights)
  uniformBuffer.load(byteBuffer)
}