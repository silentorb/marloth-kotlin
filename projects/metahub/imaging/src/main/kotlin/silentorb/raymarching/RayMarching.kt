package silentorb.raymarching

import mythic.spatial.Vector3

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

val zeroNormal: Normal = { Vector3.zero }

fun projectPoint(ray: Ray, depth: Float): Vector3 =
    ray.position + ray.direction * depth

tailrec fun march(marcher: Marcher, sdf: Sdf, ray: Ray, depth: Float, steps: Int): PointDistance {

  val point = projectPoint(ray, depth)
  val distance = sdf(point)

  // If this is too small it will pass through smaller objects
  val rayHitRange = 0.001f

  return if (distance.value < rayHitRange)
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

fun mixColorAndLuminance(color: Vector3, luminance: Float): Vector3 {
  val max = getBiggest(color)
  val min = getSmallest(color)
  val gap = max - min
  val saturationMod = 2.0f

  return color * luminance // * gap * saturationMod
}