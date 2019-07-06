package physics

import com.badlogic.gdx.math.Matrix4
import mythic.ent.Id
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.Deck
import simulation.World

fun syncWorldToBullet(bulletState: BulletState): (World) -> World = { world ->
  val quat = com.badlogic.gdx.math.Quaternion()
  world.copy(
      deck = world.deck.copy(
          bodies = world.deck.bodies.mapValues { (key, body) ->
            val btBody = bulletState.dynamicBodies[key]
            if (btBody == null)
              body
            else {
              val worldTransform = btBody.worldTransform
              val transform = worldTransform.getValues()
              worldTransform.getRotation(quat)
              body.copy(
                  position = Vector3(transform[Matrix4.M03], transform[Matrix4.M13], transform[Matrix4.M23]),
                  orientation = Quaternion(quat.x, quat.y, quat.z, quat.w)
              )
            }
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
