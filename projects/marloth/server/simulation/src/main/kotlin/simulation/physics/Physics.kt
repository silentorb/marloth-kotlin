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
import simulation.main.Deck
import simulation.main.World
import simulation.physics.old.LinearForce
import java.util.*
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

fun applyImpulses(world: World, bulletState: BulletState, linearForces: List<LinearForce>) {
  for (force in linearForces) {
    val btBody = bulletState.dynamicBodies[force.body]!!
    btBody.applyCentralImpulse(toGdxVector3(force.offset))
  }
}

fun updateBulletPhysics(bulletState: BulletState, linearForces: List<LinearForce>): (World) -> World = { world ->
  syncNewBodies(world, bulletState)
  applyImpulses(world, bulletState, linearForces)
  bulletState.dynamicsWorld.stepSimulation(1f / 60f, 10)
  syncWorldToBullet(bulletState)(world)
}

fun releaseBulletState(bulletState: BulletState) {
  bulletState.dynamicsWorld.release()
  bulletState.dynamicBodies = mapOf()
}
