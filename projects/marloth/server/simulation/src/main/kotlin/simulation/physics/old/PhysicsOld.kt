package simulation.physics.old

import mythic.ent.Id
import mythic.ent.Table
import mythic.spatial.*
import simulation.main.World
import simulation.physics.Body

data class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
)

data class LinearImpulse(
    val body: Id,
    val offset: Vector3
)

data class AbsoluteOrientationForce(
    val body: Id,
    val orientation: Quaternion
)

data class Collision(
    val first: Id,
    val second: Id,
    val hitPoint: Vector3? = null
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
        diff.normalize() * maxChange
      else
        diff

      current + adjustment
    }
  } else
    current
}

fun transitionVector(negativeMaxChange: Float, positiveMaxChange: Float, current: Vector2, target: Vector2): Vector2 {
  val diff = target - current
  val diffLength = diff.length()
  val maxChange = if (current.length() < target.length())
    positiveMaxChange
  else
    negativeMaxChange

  return if (diffLength != 0f) {
    if (diffLength < maxChange)
      target
    else {
      val adjustment = if (diffLength > maxChange)
        diff.normalize() * maxChange
      else
        diff

      current + adjustment
    }
  } else
    current
}

fun updateBody(body: Body, orientationForces: List<AbsoluteOrientationForce>): Body {
  return body.copy(
      orientation = orientationForces.firstOrNull()?.orientation ?: body.orientation
  )
}

fun updatePhysicsBodies(world: World, collisions: Collisions, movementForces: List<LinearImpulse>,
                        orientationForces: List<AbsoluteOrientationForce>, delta: Float): Table<Body> {
  val updated = world.deck.dynamicBodies.mapValues { (id, dynamicBody) ->
    val body = world.deck.bodies[id]!!
    updateBody(
        body = body,
        orientationForces = orientationForces.filter { it.body == id }
    )
  }
  return world.deck.bodies.plus(updated)
}
