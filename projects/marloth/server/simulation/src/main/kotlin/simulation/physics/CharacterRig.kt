package simulation.physics

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import mythic.ent.Id
import mythic.ent.firstSortedBy
import mythic.spatial.Vector3
import simulation.entities.airLinearDamping
import simulation.entities.characterGroundBuffer
import simulation.entities.groundedLinearDamping
import simulation.main.Deck
import kotlin.math.min

private fun rayCollisionDistance(dynamicsWorld: btDiscreteDynamicsWorld, start: Vector3, end: Vector3): Float? {
//  val callback = firstRayHitNotMe(dynamicsWorld, start, end, me)
  val callback = firstRayHit(dynamicsWorld, start, end)
  return if (callback.hasHit()) {
    val collisionObject = callback.collisionObject
    val collisionObjectId = collisionObject.userData as Id
    val hitPoint = com.badlogic.gdx.math.Vector3()
    callback.getHitPointWorld(hitPoint)
    val distance = start.z - hitPoint.z
//    println(" $collisionObjectId $distance")
    return distance
  } else
    null
}

//private fun rayCollisionDistance2(dynamicsWorld: btDiscreteDynamicsWorld, character: Id, start: Vector3, end: Vector3): Float? {
//  val callback = allRayHits(dynamicsWorld, start, end)
//  if (callback.hasHit()) {
//    val collisionCount = callback.collisionObjects.size()
//    for (i in 0 until collisionCount) {
//      val collisionObject = callback.collisionObjects.atConst(i)!!
//      val id = collisionObject.userData as Id
//      if (id != character) {
//        val hitPoint = callback.getHitPointWorld().atConst(i)
//        val distance = hitPoint.z
//        println(distance)
//        return distance
//      }
//    }
//  }
//  return null
//}

fun updateCharacterStepHeight(bulletState: BulletState, deck: Deck, character: Id): Float {
  val body = deck.bodies[character]!!
  val collisionObject = deck.collisionShapes[character]!!
  val shape = collisionObject.shape
  val radius = shape.radius
  val footRadius = radius * 1.2f
  val footOffsets = listOf(
      Vector3(footRadius, 0f, 0f),
      Vector3(0f, footRadius, 0f),
      Vector3(-footRadius, 0f, 0f),
      Vector3(0f, -footRadius, 0f),

      Vector3(footRadius, footRadius, 0f),
      Vector3(footRadius, -footRadius, 0f),
      Vector3(-footRadius, footRadius, 0f),
      Vector3(-footRadius, -footRadius, 0f)
  )
  val footHeight = radius + 0.2f
  val basePosition = body.position + Vector3(0f, 0f, -shape.shapeHeight / 2f + footHeight)
//  val basePosition = body.position
  val rayLength = footHeight
//  val rayLength = 0.02f
//  val rayLength = radius
  val endOffset = Vector3(0f, 0f, -rayLength)
  val dynamicsWorld = bulletState.dynamicsWorld
  val distances = footOffsets.mapNotNull {
    val start = basePosition + it
    val end = start + endOffset
    rayCollisionDistance(dynamicsWorld, start, end)
  }

  return if (distances.any()) {
    val distance = distances.firstSortedBy { it }
    distance - footHeight
  } else
    1f
}

fun updateCharacterStepHeightWithVelocity(bulletState: BulletState, deck: Deck, character: Id): Float {
  val body = deck.bodies[character]!!
  val collisionObject = deck.collisionShapes[character]!!
  val shape = collisionObject.shape
  val radius = shape.radius
  val footRadius = radius * 1.2f
//  val footOffsets = listOf(
//      Vector3(footRadius, 0f, 0f),
//      Vector3(0f, footRadius, 0f),
//      Vector3(-footRadius, 0f, 0f),
//      Vector3(0f, -footRadius, 0f)
//  )
  val footHeight = radius + 0.2f
  val basePosition = body.position + Vector3(0f, 0f, -shape.shapeHeight / 2f + footHeight)
//  val basePosition = body.position
  val rayLength = footHeight
//  val rayLength = 0.02f
//  val rayLength = radius
  val endOffset = Vector3(0f, 0f, -rayLength)
  val dynamicsWorld = bulletState.dynamicsWorld
//  val distances = footOffsets.mapNotNull {
  val footOffset = body.velocity.normalize() * footRadius
  val start = basePosition + footOffset
  val end = start + endOffset
  val distance = rayCollisionDistance(dynamicsWorld, start, end)
//  }

  return if (distance != null) {
    distance - footHeight
  } else
    1f
}

fun updateCharacterRig(bulletState: BulletState, deck: Deck, id: Id) {
  val character = deck.characters[id]!!
  val groundDistance = character.groundDistance
  val btBody = bulletState.dynamicBodies[id]!!
  if (groundDistance < -characterGroundBuffer) {
    val stepHeight = min(0.02f, -groundDistance)
    val impulseVector = Vector3(0f, 0f, stepHeight * 1000f)
//    btBody.applyCentralImpulse(toGdxVector3(impulseVector))
    println(stepHeight)
    btBody.translate(toGdxVector3(Vector3(0f, 0f, stepHeight)))
  }

  val linearDamping = if (character.isGrounded)
    groundedLinearDamping
  else
    airLinearDamping

  btBody.setDamping(linearDamping, 0f)
}

fun updateCharacterRigs(bulletState: BulletState, deck: Deck) {
  for ((id, _) in deck.characters) {
    updateCharacterRig(bulletState, deck, id)
  }
}
