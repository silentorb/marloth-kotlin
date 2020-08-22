package marloth.clienting.rendering.marching

import silentorb.mythic.fathom.misc.DistanceFunction
import silentorb.mythic.scenery.Camera
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3

private const val normalStep = 0.001f

//fun calculateNormal(sdf: MinimalSdf, position: Vector3, hook: SdfHook): Vector3 {
//  fun accumulateDimension(offset: Vector3) =
//      sdf(position + offset) - sdf(position - offset)
//
//  (1..6).forEach { hook() }
//
//  return Vector3(
//      accumulateDimension(Vector3(0f + normalStep, 0f, 0f)),
//      accumulateDimension(Vector3(0f, 0f + normalStep, 0f)),
//      accumulateDimension(Vector3(0f, 0f, 0f + normalStep))
//  ).normalize()
//}

//val zeroNormal: Normal = { Vector3.zero }

fun projectPoint(ray: MutableRay, depth: Float): Vector3 =
    ray.position.toVector3() + ray.direction.toVector3() * depth

//fun missedPoint(ray: Ray, depth: Float) = MarchedPoint(
//    depth = depth,
//    color = Vector3.zero,
//    position = projectPoint(ray, depth),
//    normal = Vector3.zero
//)

fun cameraPerspectiveWidths(camera: Camera): CameraPerspective {
  val fov = 45f
  val fovRadians = Math.toRadians(fov.toDouble()).toFloat()
  val tangent = Math.tan((fovRadians * 0.5f).toDouble()).toFloat()
  return CameraPerspective(
      nearHalfWidth = camera.nearClip * tangent,
      farHalfWidth = camera.farClip * tangent
  )
}

fun orthogonalRay(camera: Camera, scale: Float): RayCaster = { unitPoint, ray ->
  ray.position.set(camera.location + camera.orientation * Vector3(0f, unitPoint.x, unitPoint.y))
  ray.direction.set(camera.orientation * Vector3(1f, 0f, 0f))
}

fun perspectiveRay(camera: Camera): (Vector2, MutableRay) -> Unit {
  val (near, far) = cameraPerspectiveWidths(camera)
  val positionXVector = camera.orientation * Vector3(0f, -1f, 0f)
  val positionYVector = camera.orientation * Vector3(0f, 0f, 1f)
  val directionBase = camera.orientation * Vector3(1f, 0f, 0f)
  val angleSpread = far - near
  val angleDepth = camera.farClip - camera.nearClip
  val angleRate = angleSpread / angleDepth
  val directionXVector = camera.orientation * Vector3(0f, -angleRate, 0f)
  val directionYVector = camera.orientation * Vector3(0f, 0f, angleRate)
  val positionBase = camera.location + camera.orientation * Vector3(camera.nearClip, 0f, 0f)

  return { unitPoint, ray ->
//        val nearPoint = Vector3(near, near * unitPoint.x, near * unitPoint.y)
//    val farPoint = Vector3(far, far * unitPoint.x, far * unitPoint.y)
//    val initialDirection = (farPoint - nearPoint).normalize()

//    val initialDirection = (Vector3(1f, -unitPoint.x, unitPoint.y) * angleRateX).normalize()

//    val un = near.toDouble() * unitPoint.x.toDouble()
//    val uf = far.toDouble() * unitPoint.x.toDouble()
//    val p =  uf - un
//
//    val w = unitPoint.x.toDouble() * (far.toDouble() - near.toDouble())

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

tailrec fun march(config: MarchingConfig, getDistance: DistanceFunction, ray: MutableRay, depth: Float, steps: Int = 0): Float? {
  val point = projectPoint(ray, depth)
  val (_, distance) = getDistance(point)

  return if (distance < config.rayHitTolerance)
    depth + distance
  else {
    val newDepth = depth + distance
    if (newDepth >= config.end)
      null
    else if (steps == config.maxSteps)
      null
    else
      march(config, getDistance, ray, newDepth, steps + 1)
  }
}
