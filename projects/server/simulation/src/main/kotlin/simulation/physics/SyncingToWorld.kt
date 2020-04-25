package simulation.physics

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.BulletState
import silentorb.mythic.physics.firstRayHit
import silentorb.mythic.spatial.Vector3
import simulation.main.Deck
import silentorb.mythic.physics.Collision

fun castInteractableRay(dynamicsWorld: btDiscreteDynamicsWorld, deck: Deck, player: Id): Id? {
  val body = deck.bodies[player]!!
  val characterRig = deck.characterRigs[player]!!
  val shape = deck.collisionObjects[player]!!
  val direction = characterRig.facingVector
  val start = body.position + Vector3(0f, 0f, 0.5f) + direction * shape.shape.radius
  val end = start + direction * 5f
  val callback = firstRayHit(dynamicsWorld, start, end, CollisionGroups.tangibleMask)
  if (callback != null) {
    val id = callback.collisionObject
    if (deck.interactables.containsKey(id)) {
      return id
    }
  }

  return null
}

fun getBulletCollisions(bulletState: BulletState, deck: Deck): List<Collision> {
  val dispatcher = bulletState.dynamicsWorld.dispatcher
  val manifoldCount = dispatcher.numManifolds
  val result = mutableListOf<Collision>()
  for (manifoldIndex in 0 until manifoldCount) {
    val manifold = dispatcher.getManifoldByIndexInternal(manifoldIndex)
    val contactCount = manifold.numContacts
    for (contactIndex in 0 until contactCount) {
      val contactPoint = manifold.getContactPoint(contactIndex)
      val distance = contactPoint.distance
      if (distance < 0.0f) {
        val firstBody = manifold.body0
        val secondBody = manifold.body1
        val a = firstBody.userData as Id
        val b = secondBody.userData as Id
        if (deck.triggers.containsKey(a))
          result.add(Collision(
              first = a,
              second = b
          ))
        if (deck.triggers.containsKey(b))
          result.add(Collision(
              first = b,
              second = a
          ))
      }
    }
  }

  return result
}
