package physics

import mythic.spatial.Vector3
import mythic.spatial.isZero
import mythic.spatial.times
import simulation.AbstractWorld
import simulation.changing.checkWallCollision

data class BodyWallCollision(
    val body: Body
)

data class BodyUpdateResult(
    val wallCollision: List<BodyWallCollision>
)

fun moveBody(world: AbstractWorld, body: Body, offset: Vector3, delta: Float): BodyWallCollision? {
  val (walls, newPosition) = checkWallCollision(body.position, offset * delta, world)
//    assert(!newPosition.x.isNaN() && !newPosition.y.isNaN())
  body.position = newPosition
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