package simulation.physics.old

import mythic.ent.Id
import mythic.ent.Table
import mythic.spatial.*
import simulation.main.World
import simulation.misc.Node
import simulation.misc.Realm
import simulation.physics.Body
import simulation.physics.DynamicBody

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

fun isWalkable(node: Node?) = node?.isWalkable ?: false

//fun isGroundedOnNeighborNode(realm: Realm, body: Body): Boolean {
//  val node = realm.nodeTable[body.node]!!
//  if (node.walls.none())
//    return false
//
//  val (nearestWall, distance) = node.walls
//      .map {
//        val edge = getFloor(realm.mesh.faces[it]!!)
//        Pair(it, getPointToLineDistance(body.position, edge.first, edge.second))
//      }
//      .sortedBy { it.second }
//      .first()
//
//  return distance < 0.5f && isWalkable(realm.nodeTable[getOtherNode(node.id, realm.faces[nearestWall]!!)])
//}

//fun isGrounded(realm: Realm, body: Body): Boolean {
//  return !body.gravity || realm.nodeTable[body.node]!!.isWalkable || isGroundedOnNeighborNode(realm, body)
//}

//fun moveBody(realm: Realm, body: Body, offset: Vector3, walls: List<Collision>, delta: Float): Vector3 {
//  val position = if (offset == Vector3())
//    body.position
//  else
//    checkWallCollision(MovingBody(body.radius!!, body.position), offset * delta, walls.map {
//      WallCollision3(it.wall!!, it.hitPoint, it.directGap, it.travelingGap)
//    })
//
//  return if (!isGrounded(realm, body))
//    position + Vector3(0f, 0f, -4f * delta)
//  else
//    position
//}

fun updateBody(realm: Realm, body: Body, dynamicBody: DynamicBody, movementForces: List<LinearImpulse>, collisions: List<Collision>,
               orientationForces: List<AbsoluteOrientationForce>, delta: Float): Body {
  return body.copy(
//      velocity = applyForces(body, movementForces, dynamicBody.resistance, delta),
//      position = moveBody(realm, body, body.velocity, collisions, delta),
      orientation = orientationForces.firstOrNull()?.orientation ?: body.orientation
//      node = updateBodyNode(realm, body)
  )
}

fun updatePhysicsBodies(world: World, collisions: Collisions, movementForces: List<LinearImpulse>,
                        orientationForces: List<AbsoluteOrientationForce>, delta: Float): Table<Body> {
  val updated = world.deck.dynamicBodies.mapValues { (id, dynamicBody) ->
    val body = world.deck.bodies[id]!!
    updateBody(
        world.realm,
        body = body,
        dynamicBody = dynamicBody,
        movementForces = movementForces.filter { it.body == id },
        orientationForces = orientationForces.filter { it.body == id },
        collisions = collisions.filter { it.first == id },
        delta = delta
    )
  }
  return world.deck.bodies.plus(updated)
}
