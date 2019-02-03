package silentorb.raymarching

import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.Math
import org.joml.times

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

fun missedPoint(ray: Ray, depth: Float) = MarchedPoint(
    depth = depth,
    color = Vector3.zero,
    position = projectPoint(ray, depth),
    normal = Vector3.zero
)

const private val rayHitRange = 0.001f

fun isRayHit(distance: Float): Boolean = distance < rayHitRange

tailrec fun march(marcher: Marcher, sdf: Sdf, ray: Ray, depth: Float, steps: Int): PointDistance {

  val point = projectPoint(ray, depth)
  val distance = sdf(point)

  // If this is too small it will pass through smaller objects

  return if (isRayHit(distance.value))
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

fun newCameraTransform(camera: Camera): Matrix {
  val fov = 90f
  val fovRadians = Math.toRadians(fov.toDouble()).toFloat()
  val tangent = Math.tan((fovRadians * 0.5f).toDouble()).toFloat()

}

fun pixelRenderer(marcher: Marcher, scene: Scene, cameraTransform: Matrix): (Vector2) -> MarchedPoint = { unitPoint ->
  val sdf = scene.sdf
  val camera = scene.camera
//  val half = camera.dimensions * 0.5f
//  val offset = scene.camera.orientation * Vector3(0f, x * half.x, y * half.y)
  val ray = Ray(
      position = camera.position + Vector3(unitPoint.x, 0f, unitPoint.y).transform(cameraTransform),
      direction = scene.camera.orientation * Vector3(1f, 0f, 0f)
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