package physics

import mythic.sculpting.FlexibleFace
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.isZero
import mythic.spatial.times
import org.joml.plus
import simulation.Id
import simulation.changing.checkWallCollision

data class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
)

data class MovementForce(
    val body: Body,
    val offset: Vector3,
    val maximum: Float
)

typealias Forces = List<MovementForce>

data class BodyWallCollision(
    val body: Body
)

data class Collision(
    val first: Id,
    val second: Id? = null,
    val wall: FlexibleFace? = null,
    val hitPoint: Vector2,
    val gap: Float
)

typealias Collisions = List<Collision>

data class BodyUpdateResult(
    val wallCollision: List<BodyWallCollision>
)

fun applyForces(forces: List<MovementForce>, delta: Float) {
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

fun moveBody(body: Body, offset: Vector3, delta: Float, walls: List<Collision>) {
  val newPosition = checkWallCollision(body.position, offset * delta, walls)

  body.position = newPosition
//  val resistance = body.velocity.normalize() * body.attributes.resistance * delta

  val oldVelocity = body.velocity
//  body.velocity = Vector3(body.velocity).normalize() * (body.velocity.length() - body.attributes.resistance * delta)
  body.velocity *= 1 - body.attributes.resistance * delta
//  println("" + body.hashCode() + ", " + oldVelocity.length() + ", " + body.velocity.length())
  if (body.velocity.length() < 0.01f) {
    body.velocity.zero()
  }
}

fun updateBodies(bodies: Collection<Body>, collisions: Collisions, delta: Float) {
  bodies
      .filter { it.gravity }
      .filter { !it.node.isWalkable }
      .forEach { it.position += Vector3(0f, 0f, -4f * delta) }

  val movingBodies = bodies.filter { !isZero(it.velocity) }
  movingBodies
      .forEach { body ->
        val walls = collisions.filter { it.first == body.id && it.wall != null }
        moveBody(body, body.velocity, delta, walls)
      }
}