package physics

import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3 as GdxVector3
import com.badlogic.gdx.physics.bullet.Bullet
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import mythic.ent.Id
import mythic.sculpting.ImmutableFace
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.getCenter
import simulation.Deck
import simulation.World

typealias BulletObjectMap = Map<Id, btCollisionObject>

data class BulletState(
    var dynamicsWorld: btDiscreteDynamicsWorld,
    var objectMap: BulletObjectMap,
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
  val overlappingPairCache = btDbvtBroadphase()

  ///the default constraint solver. For parallel processing you can use a different solver (see Extras/BulletMultiThreaded)
  val solver = btSequentialImpulseConstraintSolver()

  val dynamicsWorld = btDiscreteDynamicsWorld(dispatcher, overlappingPairCache, solver, collisionConfig)

  dynamicsWorld.gravity = GdxVector3(0f, 0f, -10f)

  return BulletState(
      dynamicsWorld = dynamicsWorld,
      objectMap = mapOf()
  )
}

fun createCharacterCollisionShape(height: Float, radius: Float): btCompoundShape {
  val groundShape = btBoxShape(GdxVector3(1f, 1f, 1f))
  val compoundShape = btCompoundShape()
  compoundShape.addChildShape(toGdxMatrix4(Matrix().translate(Vector3(0f, 0f, 1f))), groundShape)
  return compoundShape
}

fun createPropCollisionShape(radius: Float): btCompoundShape {
  val groundShape = btBoxShape(GdxVector3(1f, 1f, 1f))
  val compoundShape = btCompoundShape()
  compoundShape.addChildShape(toGdxMatrix4(Matrix().translate(Vector3(0f, 0f, 1f))), groundShape)
  return compoundShape
}

fun createBulletDynamicObject(body: Body, deck: Deck): btRigidBody {
  val groundShape = btBoxShape(GdxVector3(1f, 1f, 1f))
  val compoundShape = btCompoundShape()
  compoundShape.addChildShape(toGdxMatrix4(Matrix().translate(Vector3(0f, 0f, 1f))), groundShape)

//  collisionShapes.push_back(groundShape)

  val groundTransform = Matrix().translate(body.position)

  val mass = 0f

  val isDynamic = mass != 0f

  val localInertia = GdxVector3(0f, 0f, 0f)
  if (isDynamic)
    groundShape.calculateLocalInertia(mass, localInertia)

  val myMotionState = btDefaultMotionState(toGdxMatrix4(groundTransform))
  val rbInfo = btRigidBody.btRigidBodyConstructionInfo(mass, myMotionState, compoundShape, localInertia)
  val btBody = btRigidBody(rbInfo)
  return btBody
}

fun createBulletsStaticFace(face: ImmutableFace): btCollisionObject {
  val triangleMesh = btTriangleMesh()
  val center = getCenter(face.vertices)
  val vertices = face.vertices.map { toGdxVector3(it - center) }
  for (i in (1..vertices.size - 2)) {
    triangleMesh.addTriangle(vertices[0], vertices[i], vertices[i + 1])
  }
  val shape = btBvhTriangleMeshShape(triangleMesh, true)
//  val localInertia = GdxVector3(0f, 0f, 0f)
//  val myMotionState = btDefaultMotionState(toGdxMatrix4(Matrix().translate(center)))
//  val rbInfo = btRigidBody.btRigidBodyConstructionInfo(0f, myMotionState, shape, localInertia)
  val btBody = btCollisionObject()
  btBody.collisionShape = shape
  btBody.worldTransform = toGdxMatrix4(Matrix().translate(center))
  return btBody
}

fun syncBulletToWorld(world: World, bulletState: BulletState) {
  val newBtBodies = world.deck.bodies.filterKeys { !bulletState.objectMap.containsKey(it) }
      .map { (key, value) ->
        val btBody = createBulletDynamicObject(value, world.deck)
        bulletState.dynamicsWorld.addRigidBody(btBody)
        Pair(key, btBody)
      }
  bulletState.objectMap = bulletState.objectMap.plus(newBtBodies)

  if (!bulletState.isMapSynced) {
    bulletState.isMapSynced = true
    val newFaceBodies = world.realm.faces.map { entry ->
      val face = world.realm.mesh.faces[entry.key]!!
      val collisionObject = createBulletsStaticFace(face)
      bulletState.dynamicsWorld.addCollisionObject(collisionObject)
      // Not currently tracking static meshe mapping
      Pair(entry.key, collisionObject)
    }
    bulletState.objectMap = bulletState.objectMap.plus(newFaceBodies)
  }
}

fun syncWorldToBullet(bulletState: BulletState): (World) -> World = { world ->
  world
}

fun updateBulletPhysics(bulletState: BulletState): (World) -> World = { world ->
  syncBulletToWorld(world, bulletState)
  bulletState.dynamicsWorld.stepSimulation(1f / 60f, 10)
  syncWorldToBullet(bulletState)(world)
}

fun releaseBulletState(bulletState: BulletState) {
  bulletState.dynamicsWorld.release()
  bulletState.objectMap = mapOf()
}