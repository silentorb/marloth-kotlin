package silentorb.raymarching

import mythic.spatial.Vector3
import mythic.spatial.cubicIn
import mythic.spatial.epsilon

data class Camera(
    val position: Vector3,
    val direction: Vector3
)

typealias Normal = (Vector3) -> Vector3

data class PointDistance(
    val value: Float,
    val normal: Normal = zeroNormal
)

fun sceneSdf(position: Vector3): PointDistance {
  return PointDistance(position.length() - 1.0f) { it.normalize() }
}

data class Marcher(
    val end: Float,
    val maxSteps: Int
)

val zeroNormal: Normal = { Vector3.zero }

fun projectPoint(camera: Camera, depth: Float): Vector3 =
    camera.position + camera.direction * depth

tailrec fun march(marcher: Marcher, camera: Camera, depth: Float, steps: Int): PointDistance {
  val point = projectPoint(camera, depth)
  val distance = sceneSdf(point)
  return if (distance.value < epsilon)
    PointDistance(depth, distance.normal)
  else {
    val newDepth = depth + distance.value
    if (newDepth >= marcher.end)
      PointDistance(marcher.end)
    else if (steps == marcher.maxSteps)
      PointDistance(marcher.end)
    else
      march(marcher, camera, newDepth, steps + 1)
  }
}

fun illuminatePoint(depth: Float, position: Vector3, normal: Vector3): Float {
  val depthShading = 1f - depth * 0.5f
  val lightPosition = Vector3(3f, -3f, 3f)
  val dot = (lightPosition - position).normalize().dot(normal)
  val lighting = cubicIn(dot * 0.5f + 0.5f) * 2f - 1f
  val value = depthShading * 0.5f + lighting
//  val value = depthShading * 0.2f + lighting * 0.8f
  return value
//  return minMax(value, 0f, 1f)
}

data class MarchedPoint(
    val color: Vector3,
    val depth: Float,
    val position: Vector3,
    val normal: Vector3
)

fun renderSomething(marcher: Marcher, camera: Camera): (Float, Float) -> MarchedPoint = { x, y ->
  val pixelCamera = camera.copy(
      position = Vector3(x * 4f - 2f, -2f, y * 4f - 2f)
  )
  val hit = march(marcher, pixelCamera, 0f, 0)
  if (hit.value < marcher.end) {
    val position = projectPoint(pixelCamera, hit.value)
//    val lightMod = illuminatePoint(hit.value, point, hit.normal(point))
    MarchedPoint(
        depth = hit.value,
        color = Vector3(1f, 0f, 0f),
        position = position,
        normal = hit.normal(position)
    )
  } else
    MarchedPoint(
        depth = hit.value,
        color = Vector3.zero,
        position = projectPoint(pixelCamera, hit.value),
        normal = Vector3.zero
    )
}

fun mixScenePoint(color: Vector3, illumination: Float): Vector3 =
    color * illumination