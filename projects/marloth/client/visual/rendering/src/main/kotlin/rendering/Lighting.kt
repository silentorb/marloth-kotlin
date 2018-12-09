package rendering

import mythic.glowing.UniformBuffer
import rendering.shading.createLightBuffer
import scenery.Light

fun updateLights(lights: List<Light>, uniformBuffer: UniformBuffer) {
  val byteBuffer = createLightBuffer(lights)
  uniformBuffer.load(byteBuffer)
}