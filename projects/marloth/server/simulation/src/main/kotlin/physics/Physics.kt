package physics

import mythic.ent.Id
import mythic.sculpting.ImmutableFace
import mythic.spatial.*
import simulation.*

data class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
)

data class MovementForce(
    val body: Id,
    val offset: Vector3
)

data class AbsoluteOrientationForce(
    val body: Id,
    val orientation: Quaternion
)

data class Collision(
    val first: Id,
    val second: Id? = null,
    val wall: ImmutableFace? = null,
    val hitPoint: Vector2,
    val directGap: Float,
    val travelingGap: Float
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

fun applyForces(body: Body, forces: List<MovementForce>, resistance: Float, delta: Float): Vector3 {
  if (body.perpetual)
    return body.velocity

  if (forces.size > 2)
    throw Error("Not yet supported")

  val target = if (forces.any())
    forces.first().offset
  else
    Vector3()

  return transitionVector(maxMoveVelocityChange(), body.velocity, target)
}

fun isWalkable(node: Node?) = node?.isWalkable ?: false

fun isGroundedOnNeighborNode(realm: Realm, body: Body): Boolean {
  val node = realm.nodeTable[body.node]!!
  if (node.walls.none())
    return false

  val (nearestWall, distance) = node.walls
      .map {
        val edge = getFloor(realm.mesh.faces[it]!!)
        Pair(it, getPointToLineDistance(body.position, edge.first, edge.second))
      }
      .sortedBy { it.second }
      .first()

  return distance < 0.5f && isWalkable(realm.nodeTable[getOtherNode(node, realm.faces[nearestWall]!!)])
}

fun isGrounded(realm: Realm, body: Body): Boolean {
  return !body.gravity || realm.nodeTable[body.node]!!.isWalkable || isGroundedOnNeighborNode(realm, body)
}

fun moveBody(realm: Realm, body: Body, offset: Vector3, walls: List<Collision>, delta: Float): Vector3 {
  val position = if (offset == Vector3())
    body.position
  else
    checkWallCollision(MovingBody(body.radius!!, body.position), offset * delta, walls.map {
      WallCollision3(it.wall!!, it.hitPoint, it.directGap, it.travelingGap)
    })

  return if (!isGrounded(realm, body))
    position + Vector3(0f, 0f, -4f * delta)
  else
    position
}

fun updateBody(realm: Realm, body: Body, movementForces: List<MovementForce>, collisions: List<Collision>,
               orientationForces: List<AbsoluteOrientationForce>, delta: Float): Body {
  return body.copy(
      velocity = applyForces(body, movementForces, body.attributes.resistance, delta),
      position = moveBody(realm, body, body.velocity, collisions, delta),
      orientation = orientationForces.firstOrNull()?.orientation ?: body.orientation,
      node = updateBodyNode(realm, body)
  )
}

fun updatePhysicsBodies(world: World, collisions: Collisions, movementForces: List<MovementForce>,
                        orientationForces: List<AbsoluteOrientationForce>, delta: Float): Table<Body> {
  return world.deck.bodies.mapValues { (_, body) ->
    updateBody(
        world.realm,
        body = body,
        movementForces = movementForces.filter { it.body == body.id },
        orientationForces = orientationForces.filter { it.body == body.id },
        collisions = collisions.filter { it.first == body.id && it.wall != null },
        delta = delta
    )
  }
}
