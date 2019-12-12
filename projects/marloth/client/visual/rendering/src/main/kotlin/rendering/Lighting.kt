package rendering

import mythic.glowing.UniformBuffer
import rendering.shading.LightingConfig
import rendering.shading.createLightBuffer
import silentorb.mythic.scenery.Light

fun updateLights(config: LightingConfig, lights: List<Light>, uniformBuffer: UniformBuffer) {
  val byteBuffer = createLightBuffer(config, lights)
  uniformBuffer.load(byteBuffer)
}
