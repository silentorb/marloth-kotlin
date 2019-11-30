package simulation.physics

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import mythic.ent.Id
import mythic.spatial.Vector3

interface WorldQuerySource {
  fun rayCollisionDistance(start: Vector3, end: Vector3): Float?
}

fun rayCollisionDistance(dynamicsWorld: btDiscreteDynamicsWorld, start: Vector3, end: Vector3): Float? {
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

class BulletQuerySource(val bulletState: BulletState) : WorldQuerySource {
  override fun rayCollisionDistance(start: Vector3, end: Vector3): Float? =
    rayCollisionDistance(bulletState.dynamicsWorld, start, end)

}
