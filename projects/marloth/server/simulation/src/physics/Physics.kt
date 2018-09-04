package physics

import mythic.sculpting.FlexibleFace
import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import simulation.Id
import simulation.changing.checkWallCollision
import simulation.maxMoveVelocityChange

data class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
)

data class MovementForce(
    val body: Id,
    val offset: Vector3,
    val maximum: Float
)

data class AbsoluteOrientationForce(
    val body: Id,
    val orientation: Quaternion
)

data class Collision(
    val first: Id,
    val second: Id? = null,
    val wall: FlexibleFace? = null,
    val hitPoint: Vector2,
    val gap: Float
)

typealias Collisions = List<Collision>

fun transitionVector(maxChange: Float, current: Vector3, target: Vector3): Vector3 {
  val diff = target - current
  val diffLength = diff.length()
  return if (diffLength != 0f) {
    if (diffLength < maxChange)
      target
    else {
      val adjustment = if (diffLength > maxChange)
        diff.normalize() *maxChange
      else
        diff

      current + adjustment
    }
  } else
    current
}

fun applyForces(previousVelocity: Vector3, forces: List<MovementForce>, resistance: Float, delta: Float): Vector3 {
  if (forces.size > 2)
    throw Error("Not yet supported")

  val target = if (forces.any())
    forces.first().offset
  else
    Vector3()

  return transitionVector(maxMoveVelocityChange(), previousVelocity, target)

//  val intermediate = forces.fold(previousVelocity) { a, force ->
//    val newVelocity = a + force.offset * 20f * delta
//    val length = newVelocity.length()
////    println("* " + length)
//    if (length > force.maximum)
//      newVelocity.normalize() * force.maximum
//    else
//      newVelocity
//  }
//
//  return if (intermediate.length() < 0.01f)
//    Vector3()
//  else
//    intermediate * (1f - resistance * delta)
}

fun moveBody(body: Body, offset: Vector3, walls: List<Collision>, delta: Float): Vector3 {
  val position = if (offset == Vector3())
    body.position
  else
    checkWallCollision(body.position, offset * delta, walls)

  return if (body.gravity && !body.node.isWalkable)
    position + Vector3(0f, 0f, -4f * delta)
  else
    position
}

fun updateBody(body: Body, movementForces: List<MovementForce>, collisions: List<Collision>,
               orientationForces: List<AbsoluteOrientationForce>, delta: Float): Body {
  return body.copy(
      velocity = applyForces(body.velocity, movementForces, body.attributes.resistance, delta),
      position = moveBody(body, body.velocity, collisions, delta),
      orientation = orientationForces.firstOrNull()?.orientation ?: body.orientation,
      node = updateBodyNode(body)
  )
}

fun updatePhysicsBodies(bodies: Collection<Body>, collisions: Collisions, movementForces: List<MovementForce>,
                        orientationForces: List<AbsoluteOrientationForce>, delta: Float): List<Body> {
  return bodies.map { body ->
    updateBody(
        body = body,
        movementForces = movementForces.filter { it.body == body.id },
        orientationForces = orientationForces.filter { it.body == body.id },
        collisions = collisions.filter { it.first == body.id && it.wall != null },
        delta = delta
    )
  }
}
