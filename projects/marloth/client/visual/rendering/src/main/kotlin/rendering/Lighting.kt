package rendering

import mythic.glowing.UniformBuffer
import mythic.spatial.putVector3m
import mythic.spatial.putVector4
import org.lwjgl.BufferUtils
import scenery.Light
import java.nio.ByteBuffer

fun updateLights(lights: List<Light>, uniformBuffer: UniformBuffer) {
  val byteBuffer = createLightBuffer(lights)
  uniformBuffer.load(byteBuffer)
}