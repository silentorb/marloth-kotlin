package simulation.physics

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import mythic.ent.Id
import mythic.ent.firstSortedBy
import mythic.spatial.Vector3
import mythic.spatial.createArcZ
import simulation.entities.airLinearDamping
import simulation.entities.groundedLinearDamping
import simulation.main.Deck
import kotlin.math.abs
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

fun footOffsets(radius: Float): List<Vector3> =
    createArcZ(radius + 0.9f, 8)

fun updateCharacterStepHeight(bulletState: BulletState, deck: Deck, character: Id): Float {
  val body = deck.bodies[character]!!
  val collisionObject = deck.collisionShapes[character]!!
  val shape = collisionObject.shape
  val radius = shape.radius
  val footHeight = 0.8f
  val basePosition = body.position + Vector3(0f, 0f, -shape.height / 2f + footHeight)
//  val basePosition = body.position
  val rayLength = footHeight + 0.3f
//  val rayLength = 0.02f
//  val rayLength = radius
  val endOffset = Vector3(0f, 0f, -rayLength)
  val dynamicsWorld = bulletState.dynamicsWorld
  val offsets = footOffsets(radius).plus(Vector3.zero)
  val distances = offsets
      .mapNotNull {
        val start = basePosition + it
        val end = start + endOffset
        rayCollisionDistance(dynamicsWorld, start, end)
      }

  return if (distances.any()) {
    val distance = distances.firstSortedBy { it }
    distance - footHeight
  } else
    1f
//  } else {
//    val endOffset2 = Vector3(0f, 0f, -rayLength * 2f)
//    val distances = offsets
//        .mapNotNull {
//          val start = basePosition + it
//          val end = start + endOffset2
//          rayCollisionDistance(dynamicsWorld, start, end)
//        }
//    1f
//  }
}

fun updateCharacterRig(bulletState: BulletState, deck: Deck, id: Id) {
  val character = deck.characters[id]!!
  val groundDistance = character.groundDistance
  val btBody = bulletState.dynamicBodies[id]!!
//  if (character.isGrounded && abs(groundDistance) > 0.01f) {
  if (character.isGrounded && (groundDistance < -0.01f || groundDistance > 0.1f)) {
//    if (groundDistance < -characterGroundBuffer) {
//    val stepHeight = min(10.04f, -groundDistance -characterGroundBuffer)
    val stepHeight = -groundDistance
//    val impulseVector = Vector3(0f, 0f, stepHeight * 1000f)
//    btBody.applyCentralImpulse(toGdxVector3(impulseVector))

    val body = deck.bodies[id]!!
    println("$stepHeight ${body.position.z}")
    if (stepHeight < 0f) {
      val k = 0
    }
    val transitionStepHeight = min(0.03f, stepHeight)
    btBody.translate(toGdxVector3(Vector3(0f, 0f, transitionStepHeight)))
//    else
//      println("nothing $stepHeight")
//    } else
//      println("nothing $groundDistance ${character.isGrounded}")
  }

  val linearDamping = if (character.isGrounded)
    groundedLinearDamping
  else
    airLinearDamping

  val gravity = if (character.isGrounded)
    com.badlogic.gdx.math.Vector3.Zero
  else
    staticGravity()

  btBody.setDamping(linearDamping, 0f)
  btBody.gravity = gravity
}

fun updateCharacterRigs(bulletState: BulletState, deck: Deck) {
  for ((id, _) in deck.characters) {
    updateCharacterRig(bulletState, deck, id)
  }
}
