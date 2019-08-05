package simulation.physics

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import mythic.ent.Id
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import simulation.main.World
import simulation.physics.old.LinearImpulse
import com.badlogic.gdx.math.Vector3 as GdxVector3

// TODO: Migrate to LWJGL Bullet Bindings if it ever seems a little more used and documented.

data class BulletState(
    var dynamicsWorld: btDiscreteDynamicsWorld,
    var dynamicBodies: Map<Id, btRigidBody>,
    var staticBodies: Map<Id, btCollisionObject>,
//    var collisionObjectMap: Map<Int, Id>,
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

fun staticGravity() = GdxVector3(0f, 0f, -10f)

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
  dynamicsWorld.gravity = staticGravity()

  return BulletState(
      dynamicsWorld = dynamicsWorld,
      dynamicBodies = mapOf(),
      staticBodies = mapOf()
  )
}

fun releaseBulletState(bulletState: BulletState) {
  bulletState.dynamicsWorld.release()
  bulletState.dynamicBodies = mapOf()
}

fun firstRayHit(dynamicsWorld: btDiscreteDynamicsWorld, start: Vector3, end: Vector3): ClosestRayResultCallback {
  val start2 = toGdxVector3(start)
  val end2 = toGdxVector3(end)
  val callback = ClosestRayResultCallback(start2, end2)
  dynamicsWorld.collisionWorld.rayTest(start2, end2, callback)
  return callback
}

// ClosestNotMeRayResultCallback is REAAAALLY slow.  Must be horribly unoptimized.
fun firstRayHitNotMe(dynamicsWorld: btDiscreteDynamicsWorld, start: Vector3, end: Vector3, collisionObject: btCollisionObject): ClosestNotMeRayResultCallback {
  val start2 = toGdxVector3(start)
  val end2 = toGdxVector3(end)
  val callback = ClosestNotMeRayResultCallback(collisionObject)
  dynamicsWorld.collisionWorld.rayTest(start2, end2, callback)
  return callback
}


fun allRayHits(dynamicsWorld: btDiscreteDynamicsWorld, start: Vector3, end: Vector3): AllHitsRayResultCallback {
  val callback = AllHitsRayResultCallback(com.badlogic.gdx.math.Vector3.Zero, com.badlogic.gdx.math.Vector3.Z)
  dynamicsWorld.collisionWorld.rayTest(toGdxVector3(start), toGdxVector3(end), callback)
  return callback
}

fun updateBulletPhysics(bulletState: BulletState, linearForces: List<LinearImpulse>): (World) -> World = { world ->
  syncNewBodies(world, bulletState)
  syncRemovedBodies(world, bulletState)
  updateCharacterRigs(bulletState, world.deck)
  applyImpulses(world, bulletState, linearForces)
  bulletState.dynamicsWorld.stepSimulation(1f / 60f, 10)
  syncWorldToBullet(bulletState)(world)
}
