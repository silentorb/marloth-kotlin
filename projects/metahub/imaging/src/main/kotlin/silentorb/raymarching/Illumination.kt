package silentorb.raymarching

import mythic.spatial.Vector3
import mythic.spatial.cubicIn

data class Light(
    val position: Vector3,
    val brightness: Float
)

fun illuminatePoint(lights: List<Light>, depth: Float, position: Vector3, normal: Vector3): Float {
  val depthShading = 1f - depth * 0.5f
  val lightValues = lights
      .map { light ->
        val dot = (light.position - position).normalize().dot(normal)
        cubicIn(dot * 0.5f + 0.5f) * light.brightness
      }

  val lighting = lightValues.sum()
  val value = depthShading * 0.5f + lighting
  return value
}
