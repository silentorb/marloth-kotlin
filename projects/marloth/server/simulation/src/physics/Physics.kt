package physics

import mythic.spatial.Vector3
import mythic.spatial.isZero
import mythic.spatial.times
import org.joml.minus
import org.joml.plus
import simulation.AbstractWorld
import simulation.changing.checkWallCollision

data class Force(
    val body: Body,
    val offset: Vector3,
    val maximum: Float
)

data class BodyWallCollision(
    val body: Body
)

data class BodyUpdateResult(
    val wallCollision: List<BodyWallCollision>
)

fun applyForces(forces: List<Force>, delta: Float) {
//  val groups = forces.groupBy { it.body }
  for (force in forces) {
    val body = force.body
    body.velocity += force.offset * 20f * delta
    val length = body.velocity.length()
//    println("* " + length)
    if (length > force.maximum) {
      body.velocity = Vector3(body.velocity).normalize() * force.maximum
    }
  }
}

fun moveBody(world: AbstractWorld, body: Body, offset: Vector3, delta: Float): BodyWallCollision? {
  val (walls, newPosition) = checkWallCollision(body.position, offset * delta, world, body.node)
//    assert(!newPosition.x.isNaN() && !newPosition.y.isNaN())
  body.position = newPosition
//  val resistance = body.velocity.normalize() * body.attributes.resistance * delta

  val oldVelocity = body.velocity
//  body.velocity = Vector3(body.velocity).normalize() * (body.velocity.length() - body.attributes.resistance * delta)
  body.velocity *= 1 - body.attributes.resistance * delta
//  println("" + body.hashCode() + ", " + oldVelocity.length() + ", " + body.velocity.length())
  if (body.velocity.length() < 0.01f) {
    body.velocity.zero()
  }
  return if (walls.any())
    BodyWallCollision(body)
  else
    null
}

fun updateBodies(world: AbstractWorld, bodies: Collection<Body>, delta: Float): BodyUpdateResult {
  val movingBodies = bodies.filter { !isZero(it.velocity) }
  val collisions = movingBodies
      .mapNotNull { moveBody(world, it, it.velocity, delta) }

  return BodyUpdateResult(collisions)
}