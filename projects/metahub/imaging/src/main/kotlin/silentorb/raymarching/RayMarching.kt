package silentorb.raymarching

import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.Math
import org.joml.times

private const val normalStep = 0.001f

fun calculateNormal(sdf: MinimalSdf, position: Vector3, hook: SdfHook): Vector3 {
  fun accumulateDimension(offset: Vector3) =
      sdf(position + offset) - sdf(position - offset)

  (1..6).forEach { hook() }

  return Vector3(
      accumulateDimension(Vector3(0f + normalStep, 0f, 0f)),
      accumulateDimension(Vector3(0f, 0f + normalStep, 0f)),
      accumulateDimension(Vector3(0f, 0f, 0f + normalStep))
  ).normalize()
}

//val zeroNormal: Normal = { Vector3.zero }

fun projectPoint(ray: Ray, depth: Float): Vector3 =
    ray.position + ray.direction * depth

fun missedPoint(ray: Ray, depth: Float) = MarchedPoint(
    depth = depth,
    color = Vector3.zero,
    position = projectPoint(ray, depth),
    normal = Vector3.zero
)

const private val rayHitRange = 0.001f

fun cameraPerspectiveWidths(camera: Camera): CameraPerspective {
  val fov = 45f
  val fovRadians = Math.toRadians(fov.toDouble()).toFloat()
  val tangent = Math.tan((fovRadians * 0.5f).toDouble()).toFloat()
  return CameraPerspective(
      nearHalfWidth = camera.near * tangent,
      farHalfWidth = camera.far * tangent
  )
}

fun orthogonalRay(camera: Camera, scale: Float): (Vector2) -> Ray = { unitPoint ->
  Ray(
      position = camera.position + camera.orientation * Vector3(0f, unitPoint.x, unitPoint.y),
      direction = camera.orientation * Vector3(1f, 0f, 0f)
  )
}

fun perspectiveRay(camera: Camera): (Vector2) -> Ray {
  val (near, far) = cameraPerspectiveWidths(camera)
  return { unitPoint ->
    val nearPoint = Vector3(near, near * unitPoint.x, near * unitPoint.y)
    val farPoint = Vector3(far, far * unitPoint.x, far * unitPoint.y)
    val initialDirection = (farPoint - nearPoint).normalize()
    Ray(
        position = camera.position + camera.orientation * nearPoint,
        direction = camera.orientation * initialDirection
    )
  }
}

fun isRayHit(distance: Float): Boolean = distance < rayHitRange

fun march(marcher: Marcher, sdf: Sdf, ray: Ray, depth: Float, hook: SdfHook, steps: Int): PointDistance {

  val point = projectPoint(ray, depth)
  val distance = sdf(hook, point)

  // If this is too small it will pass through smaller objects

  return if (isRayHit(distance.value))
    PointDistance(depth, distance.geometry)
  else {
    val newDepth = depth + distance.value
    if (newDepth >= marcher.end)
      PointDistance(marcher.end)
    else if (steps == marcher.maxSteps)
      PointDistance(marcher.end)
    else
      march(marcher, sdf, ray, newDepth, hook, steps + 1)
  }
}

typealias PixelRenderer = (Vector2) -> MarchedPoint

fun pixelRenderer(marcher: Marcher, scene: Scene, cast: RayCaster, hook: SdfHook, normalHook: SdfHook): PixelRenderer = { unitPoint ->
  val ray = cast(unitPoint)
  val sdf = scene.sdf(ray)
  if (unitPoint.x == 0f && unitPoint.y == 0f) {
    val k = 0
  }

  val hit = march(marcher, sdf, ray, 0f, hook, 0)
  if (hit.value < marcher.end) {
    val position = projectPoint(ray, hit.value)
    MarchedPoint(
        depth = hit.value,
        color = Vector3(1f, 0f, 0f),
        position = position,
        normal = if (hit.geometry != null) hit.geometry.normal(normalHook, position) else Vector3.zero
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