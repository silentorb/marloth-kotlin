package physics

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import mythic.ent.Id
import mythic.spatial.Matrix
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.*
import com.badlogic.gdx.math.Vector3 as GdxVector3

data class BulletState(
    var dynamicsWorld: btDiscreteDynamicsWorld,
    var dynamicBodies: Map<Id, btRigidBody>,
    var staticBodies: Map<Id, btCollisionObject>,
    var isMapSynced: Boolean = false
)

fun toGdxVector3(vector: Vector3) = GdxVector3(vector.x, vector.y, vector.z)
fun toVector3(vector: GdxVector3) = Vector3(vector.x, vector.y, vector.z)

fun toGdxMatrix4(matrix: Matrix): Matrix4 {
  val result = Matrix4()
  val values = FloatArray(16)
  matrix.get(values)
  result.set(values)
  return result
}

private var isBulletInitialized = false

fun newBulletState(): BulletState {
  if (!isBulletInitialized) {
    Bullet.init()
    isBulletInitialized = true
  }

  val collisionConfig = btDefaultCollisionConfiguration()

  ///use the default collision dispatcher. For parallel processing you can use a diffent dispatcher (see Extras/BulletMultiThreaded)
  val dispatcher = btCollisionDispatcher(collisionConfig)

  ///btDbvtBroadphase is a good general purpose broadphase. You can also try out btAxis3Sweep.
  val broadphase = btDbvtBroadphase()

  ///the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
  val solver = btSequentialImpulseConstraintSolver()

  val dynamicsWorld = btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig)
  dynamicsWorld.gravity = GdxVector3(0f, 0f, -10f)

  return BulletState(
      dynamicsWorld = dynamicsWorld,
      dynamicBodies = mapOf(),
      staticBodies = mapOf()
  )
}

fun applyImpulses(world: World, bulletState: BulletState) {
  world.deck.bodies.forEach { (_, body) ->
    if (body.velocity != Vector3.zero) {
      val btBody = bulletState.dynamicBodies[body.id]
      if (btBody != null) {
        btBody.applyCentralImpulse(toGdxVector3(body.velocity))
      } else {
        val warningVelocitySetOnUnsyncedBody = 1
      }
    }
  }
}

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

fun updateBulletPhysics(bulletState: BulletState): (World) -> World = { world ->
  syncNewBodies(world, bulletState)
  applyImpulses(world, bulletState)
  bulletState.dynamicsWorld.stepSimulation(1f / 60f, 10)
  syncWorldToBullet(bulletState)(world)
}

fun releaseBulletState(bulletState: BulletState) {
  bulletState.dynamicsWorld.release()
  bulletState.dynamicBodies = mapOf()
}
