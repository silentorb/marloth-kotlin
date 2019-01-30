package silentorb.raymarching

import mythic.spatial.*

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
  val lighting = cubicIn(dot * 0.5f + 0.5f)
  val value = depthShading * 0.2f + lighting * 0.8f
  return minMax(value, 0f, 1f)
}

fun renderSomething(): (Float, Float) -> Vector3 = { x, y ->
  val marcher = Marcher(
      end = 2f,
      maxSteps = 100
  )

  val camera = Camera(
      position = Vector3(x * 4f - 2f, -2f, y * 4f - 2f),
      direction = Vector3(0f, 1f, 0f).normalize()
  )
  val hit = march(marcher, camera, 0f, 0)
  if (hit.value < marcher.end) {
    val point = projectPoint(camera, hit.value)
    val lightMod = illuminatePoint(hit.value, point, hit.normal(point))
    Vector3(1f, 0f, 0f) * lightMod
  } else
    Vector3(0f, 0f, 0f)
}
