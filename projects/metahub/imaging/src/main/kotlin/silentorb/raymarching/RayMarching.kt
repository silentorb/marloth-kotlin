package silentorb.raymarching

import mythic.spatial.Vector3
import mythic.spatial.cubicIn
import mythic.spatial.epsilon

data class Camera(
    val position: Vector3,
    val direction: Vector3
)

data class Ray(
    val position: Vector3,
    val direction: Vector3
)

typealias Normal = (Vector3) -> Vector3

data class PointDistance(
    val value: Float,
    val normal: Normal = zeroNormal
)

typealias Sdf = (Vector3) -> PointDistance

private const val normalStep = 0.001f

fun calculateNormal(sdf: Sdf, position: Vector3): Vector3 {
  fun accumulateDimension(offset: Vector3) =
      sdf(position + offset).value - sdf(position - offset).value

  return Vector3(
      accumulateDimension(Vector3(0f + normalStep, 0f, 0f)),
      accumulateDimension(Vector3(0f, 0f + normalStep, 0f)),
      accumulateDimension(Vector3(0f, 0f, 0f + normalStep))
  ).normalize()
}

fun sceneSdf(position: Vector3): PointDistance {
  return PointDistance(position.length() - 1.0f)
}

data class Marcher(
    val end: Float,
    val maxSteps: Int
)

data class Scene(
    val sdf: Sdf,
    val camera: Camera
)

val zeroNormal: Normal = { Vector3.zero }

fun projectPoint(ray: Ray, depth: Float): Vector3 =
    ray.position + ray.direction * depth

tailrec fun march(marcher: Marcher, sdf: Sdf, ray: Ray, depth: Float, steps: Int): PointDistance {

  val point = projectPoint(ray, depth)
  val distance = sdf(point)

  return if (distance.value < epsilon)
    PointDistance(depth, distance.normal)
  else {
    val newDepth = depth + distance.value
    if (newDepth >= marcher.end)
      PointDistance(marcher.end)
    else if (steps == marcher.maxSteps)
      PointDistance(marcher.end)
    else
      march(marcher, sdf, ray, newDepth, steps + 1)
  }
}

fun illuminatePoint(depth: Float, position: Vector3, normal: Vector3): Float {
  val depthShading = 1f - depth * 0.5f
  val lightPosition = Vector3(3f, -3f, 3f)
  val dot = (lightPosition - position).normalize().dot(normal)
  val lighting = cubicIn(dot * 0.5f + 0.5f) * 2f - 1f
  val value = depthShading * 0.5f + lighting
  return value
}

data class MarchedPoint(
    val color: Vector3,
    val depth: Float,
    val position: Vector3,
    val normal: Vector3
)

fun missedPoint(ray: Ray, depth: Float) = MarchedPoint(
    depth = depth,
    color = Vector3.zero,
    position = projectPoint(ray, depth),
    normal = Vector3.zero
)

fun pixelRenderer(marcher: Marcher, scene: Scene): (Float, Float) -> MarchedPoint = { x, y ->
  val sdf = scene.sdf
  val ray = Ray(
      position = Vector3(x * 4f - 2f, -2f, y * 4f - 2f),
      direction = scene.camera.direction
  )
  val hit = march(marcher, sdf, ray, 0f, 0)
  if (hit.value < marcher.end) {
    val position = projectPoint(ray, hit.value)
    MarchedPoint(
        depth = hit.value,
        color = Vector3(1f, 0f, 0f),
        position = position,
        normal = calculateNormal(scene.sdf, position)
    )
  } else
    missedPoint(ray, hit.value)
}

fun mixScenePoint(color: Vector3, illumination: Float): Vector3 =
    color * illumination