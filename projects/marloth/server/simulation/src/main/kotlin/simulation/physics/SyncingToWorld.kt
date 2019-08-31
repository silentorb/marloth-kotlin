package simulation.physics

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import mythic.ent.Id
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.main.Deck
import simulation.main.World
import simulation.main.defaultPlayer
import simulation.physics.old.Collision

fun castInteractableRay(dynamicsWorld: btDiscreteDynamicsWorld, deck: Deck, player: Id): Id? {
  val body = deck.bodies[player]!!
  val character = deck.characters[player]!!
  val shape = deck.collisionShapes[player]!!
  val direction = character.facingVector
  val start = body.position + Vector3(0f, 0f, 0.5f) + direction * shape.shape.radius
  val end = start + direction * 5f
  val callback = firstRayHit(dynamicsWorld, start, end)
  if (callback.hasHit()) {
    val collisionObject = callback.collisionObject
    val id = collisionObject.userData as Id
    if (deck.interactables.containsKey(id)) {
      return id
    }
  }

  return null
}

fun syncWorldToBullet(bulletState: BulletState): (World) -> World = { world ->
  val quat = com.badlogic.gdx.math.Quaternion()
  val deck = world.deck
  val player = defaultPlayer(deck)
  world.copy(
      deck = deck.copy(
          bodies = deck.bodies.mapValues { (key, body) ->
            val btBody = bulletState.dynamicBodies[key]
            if (btBody == null)
              body
            else {
              val worldTransform = btBody.worldTransform
              val transform = worldTransform.getValues()
              worldTransform.getRotation(quat)
              body.copy(
                  position = Vector3(transform[Matrix4.M03], transform[Matrix4.M13], transform[Matrix4.M23]),
                  orientation = Quaternion(quat.x, quat.y, quat.z, quat.w),
                  velocity = toVector3(btBody.linearVelocity)
              )
            }
          },
          characters = deck.characters.plus(
              Pair(player, deck.characters[player]!!.copy(
                  canInteractWith = castInteractableRay(bulletState.dynamicsWorld, deck, player)
              ))
          )
              .mapValues { (id, character) ->
                character.copy(
                    groundDistance = updateCharacterStepHeight(bulletState, deck, id)
                )
              }
      )
  )
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
        else if (deck.triggers.containsKey(b))
          result.add(Collision(
              first = b,
              second = a
          ))
      }
    }
  }

  return result
}
