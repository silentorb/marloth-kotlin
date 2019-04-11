package physics

import com.badlogic.gdx.math.Matrix4
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
import simulation.*
import com.badlogic.gdx.math.Vector3 as GdxVector3

data class BulletState(
    var dynamicsWorld: btDiscreteDynamicsWorld,
    var dynamicBodies: Map<Id, btRigidBody>,
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
      dynamicBodies = mapOf()
  )
}

fun createCharacterCollisionShape(height: Float, radius: Float): btCollisionShape {
  val groundShape = btCapsuleShapeZ(radius, height)
  val compoundShape = btCompoundShape()
  compoundShape.addChildShape(toGdxMatrix4(Matrix().translate(Vector3(0f, 0f, height / 2f))), groundShape)
  return compoundShape
}

fun createPropCollisionShape(radius: Float): btCollisionShape {
  val groundShape = btSphereShape(radius)
//  val compoundShape = btCompoundShape()
//  compoundShape.addChildShape(toGdxMatrix4(Matrix().translate(Vector3(0f, 0f, 1f))), groundShape)
  return groundShape
}

fun createBulletDynamicObject(body: Body, deck: Deck): btRigidBody {
  val shape = when {
    deck.characters.containsKey(body.id) -> createCharacterCollisionShape(2f, 0.3f)
    else -> createPropCollisionShape(0.3f)
  }

//  collisionShapes.push_back(groundShape)
  val groundTransform = Matrix().translate(body.position + Vector3(0f, 0f, 1f))
  val mass = 45f
  val localInertia = GdxVector3(0f, 0f, 0f)
  val isDynamic = mass != 0f
//  if (isDynamic)
//    shape.calculateLocalInertia(mass, localInertia)

  val myMotionState = btDefaultMotionState(toGdxMatrix4(groundTransform))
  val rbInfo = btRigidBody.btRigidBodyConstructionInfo(mass, myMotionState, shape, localInertia)
  val btBody = btRigidBody(rbInfo)
  btBody.activationState = CollisionConstants.DISABLE_DEACTIVATION
  val k = btBody.friction
  btBody.friction = 0.5f
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
  val btBody = btCollisionObject()
  btBody.collisionShape = shape
  btBody.worldTransform = toGdxMatrix4(Matrix().translate(center))
  return btBody
}

fun createPropBody(body: Body, deck: Deck): btCollisionObject {
  val shape = createPropCollisionShape(1.1f)
  val btBody = btCollisionObject()
  btBody.collisionShape = shape
  btBody.worldTransform = toGdxMatrix4(Matrix().translate(body.position + Vector3(0f, 0f, 1f)))
  return btBody
}

fun syncMapGeometryAndPhysics(world: World, bulletState: BulletState) {
  val newFaceBodies = world.realm.faces
      .filterValues { it.faceType != FaceType.space && it.texture != null }
      .map { entry ->
        val face = world.realm.mesh.faces[entry.key]!!
        val collisionObject = createBulletsStaticFace(face)
        bulletState.dynamicsWorld.addCollisionObject(collisionObject)
        // Not currently tracking static meshe mapping
        Pair(entry.key, collisionObject)
      }
//    bulletState.dynamicBodies = bulletState.dynamicBodies.plus(newFaceBodies)
}

fun syncNewBodies(world: World, bulletState: BulletState) {
  val newBtBodies = world.deck.bodies
      .filterKeys { !bulletState.dynamicBodies.containsKey(it) }
//      .filterKeys { !previousBodies.containsKey(it) }
//      .filterKeys { !world.deck.doors.containsKey(it) }
      .filterKeys { world.deck.characters.containsKey(it) }
      .map { (key, value) ->
        val btBody = createBulletDynamicObject(value, world.deck)
        bulletState.dynamicsWorld.addRigidBody(btBody)
        Pair(key, btBody)
      }

  bulletState.dynamicBodies = bulletState.dynamicBodies.plus(newBtBodies)

  if (!bulletState.isMapSynced) {
    bulletState.isMapSynced = true
    syncMapGeometryAndPhysics(world, bulletState)
    val newPropBodies = world.deck.bodies
        .filterKeys { !bulletState.dynamicBodies.containsKey(it) }
        .filterKeys { world.deck.lights.containsKey(it) }
        .map { (key, value) ->
          val collisionObject = createPropBody(value, world.deck)
          bulletState.dynamicsWorld.addCollisionObject(collisionObject)
          Pair(key, collisionObject)
        }

  }
}

fun applyImpulses(world: World, bulletState: BulletState) {
  world.deck.bodies.forEach { (_, body) ->
    if (body.velocity != Vector3.zero) {
      val btBody = bulletState.dynamicBodies[body.id]
      if (btBody != null) {
        btBody.linearVelocity = toGdxVector3(body.velocity)
      }
      else {
        val warningVelocitySetOnUnsyncedBody = 1
      }
    }
  }
}

fun syncWorldToBullet(bulletState: BulletState): (World) -> World = { world ->
  world.copy(
      deck = world.deck.copy(
          bodies = world.deck.bodies.mapValues { (key, body) ->
            val btBody = bulletState.dynamicBodies[key]
            if (btBody == null)
              body
            else {
              val transform = btBody.worldTransform.getValues()
              body.copy(
                  position = Vector3(transform[Matrix4.M03], transform[Matrix4.M13], transform[Matrix4.M23])
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