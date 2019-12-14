package silentorb.raymarching

import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.cubicIn

data class Light(
    val position: Vector3,
    val brightness: Float
)

fun illuminatePoint(lights: List<Light>, depth: Float, position: Vector3, normal: Vector3): Float {
//  val depthShading = 1f - depth * 0.5f
  if (normal == Vector3.zero)
    return 0f

  var sum = 0f
  for (light in lights) {
    val dot = (light.position - position).normalize().dot(normal)
    sum += cubicIn(dot * 0.5f + 0.5f) * light.brightness
  }
  return sum
}
