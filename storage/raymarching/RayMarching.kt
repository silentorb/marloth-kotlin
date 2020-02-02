package silentorb.raymarching

import silentorb.mythic.spatial.MutableVector3
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
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
    Vector3(ray.position) + Vector3(ray.direction) * depth

//fun missedPoint(ray: Ray, depth: Float) = MarchedPoint(
//    depth = depth,
//    color = Vector3.zero,
//    position = projectPoint(ray, depth),
//    normal = Vector3.zero
//)

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

fun orthogonalRay(camera: Camera, scale: Float): RayCaster = { unitPoint, ray ->
  ray.position.set(camera.position + camera.orientation * Vector3(0f, unitPoint.x, unitPoint.y))
  ray.direction.set(camera.orientation * Vector3(1f, 0f, 0f))
}

fun perspectiveRay(camera: Camera): (Vector2, MutableRay) -> Unit {
  val (near, far) = cameraPerspectiveWidths(camera)
  val positionXVector = camera.orientation * Vector3(0f, -1f, 0f)
  val positionYVector = camera.orientation * Vector3(0f, 0f, 1f)
  val directionBase = camera.orientation * Vector3(1f, 0f, 0f)
  val angleSpread = far - near
  val angleDepth = camera.far - camera.near
  val angleRate = angleSpread / angleDepth
  val directionXVector = camera.orientation * Vector3(0f, -angleRate, 0f)
  val directionYVector = camera.orientation * Vector3(0f, 0f, angleRate)
  val positionBase = camera.position + camera.orientation * Vector3(camera.near, 0f, 0f)

  return { unitPoint, ray ->
    //    val nearPoint = Vector3(near, near * unitPoint.x, near * unitPoint.y)
//    val farPoint = Vector3(far, far * unitPoint.x, far * unitPoint.y)
//    val initialDirection = (farPoint - nearPoint).normalize()

//    val initialDirection = (Vector3(1f, -unitPoint.x, unitPoint.y) * angleRateX).normalize()

//    val un = near.toDouble() * unitPoint.x.toDouble()
//    val uf = far.toDouble() * unitPoint.x.toDouble()
//    val p =  uf - un
//
//    val w = unitPoint.x.toDouble() * (far.toDouble() - near.toDouble())
//
//    assert(h == initialDirection)

    val p = (directionBase + directionXVector * -unitPoint.x + directionYVector * unitPoint.y)
    ray.position.set(positionBase + positionXVector * unitPoint.x + positionYVector * unitPoint.y)
    ray.direction.set(p.normalize())
//    Ray(
//        position = positionBase + positionXVector * unitPoint.x + positionYVector * unitPoint.y,
//        direction = p.normalize()
//        // direction = camera.orientation * initialDirection
//    )
  }
}

fun isRayHit(distance: Float): Boolean = distance < rayHitRange

fun march(marcher: Marcher, sdf: Sdf, ray: Ray, depth: Float, hook: SdfHook, steps: Int): Float {
  return 0f

//  val point = projectPoint(ray, depth)
//  val distance = sdf(hook, point)
//
//  // If this is too small it will pass through smaller objects
//
//  return if (isRayHit(distance.value))
//    PointDistance(depth, distance.geometry)
//  else {
//    val newDepth = depth + distance.value
//    if (newDepth >= marcher.end)
//      PointDistance(marcher.end)
//    else if (steps == marcher.maxSteps)
//      PointDistance(marcher.end)
//    else
//      march(marcher, sdf, ray, newDepth, hook, steps + 1)
//  }
}

typealias PixelRenderer = (Vector2, MarchedPoint) -> Unit

fun pixelRenderer(marcher: Marcher, scene: Scene, cast: RayCaster, hook: SdfHook, normalHook: SdfHook): PixelRenderer {
  val ray = MutableRay(MutableVector3(), MutableVector3())
  return { unitPoint, marchedPoint ->
//    cast(unitPoint, ray)
//    val sdf = scene.sdf(ray)
//  if (unitPoint.x == 0f && unitPoint.y == 0f) {
//    val k = 0
//  }
//    marchedPoint.depth = 0f
//    MarchedPoint(
//        depth = 0f,
//        color = Vector3.zero,
//      position = projectPoint(ray, 0f),
////        position = Vector3.zero,
//        normal = Vector3.zero
//    )
//  MarchedPoint(
//      depth = 0f,
//      color = Vector3.zero,
//      position = Vector3.zero,
//      normal = Vector3.zero
//  )
//kj
//  missedPoint(Ray(Vector3(), Vector3()),0f)

    val depth = 0f //march(marcher, sdf, ray, 0f, hook, 0)
    if (depth < marcher.end) {
//      val position = projectPoint(ray, depth)

//      marchedPoint.depth = depth
//      marchedPoint.color.set(1f, 0f, 0f)
//      marchedPoint.position.set(0f, 0f, 0f)
//      marchedPoint.normal.set(0f, 0f, 0f)

//    MarchedPoint(
//        depth = depth,
//        color = Vector3(1f, 0f, 0f),
//        position = position,
//        normal = Vector3.zero //if (depth.geometry != null) depth.geometry.normal(normalHook, position) else Vector3.zero
//    )
    } else {
//    missedPoint(ray, hit.value)

//      marchedPoint.depth = depth
//      marchedPoint.color.set(Vector3.zero)
//      marchedPoint.position.set(projectPoint(ray, depth))
//      marchedPoint.normal.set(Vector3.zero)
    }
  }
}

fun mixColorAndLuminance(color: Vector3, luminance: Float): Vector3 {
  val max = getBiggest(color)
  val min = getSmallest(color)
  val gap = max - min
  val saturationMod = 2.0f

  return color * luminance // * gap * saturationMod
}
