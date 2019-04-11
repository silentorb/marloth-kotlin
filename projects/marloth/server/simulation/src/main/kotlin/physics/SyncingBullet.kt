package physics

import colliding.Capsule
import colliding.Shape
import colliding.ShapeOffset
import colliding.Sphere
import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import mythic.sculpting.ImmutableFace
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.getCenter
import simulation.Deck
import simulation.FaceType
import simulation.World

fun createBulletDynamicObject(body: Body, dynamicBody: DynamicBody, shape: btCollisionShape): btRigidBody {
  val groundTransform = Matrix().translate(body.position + Vector3(0f, 0f, 1f))
  val localInertia = com.badlogic.gdx.math.Vector3(0f, 0f, 0f)
//  val isDynamic = mass != 0f
//  if (isDynamic)
//    shape.calculateLocalInertia(mass, localInertia)

  val myMotionState = btDefaultMotionState(toGdxMatrix4(groundTransform))
  val rbInfo = btRigidBody.btRigidBodyConstructionInfo(dynamicBody.mass, myMotionState, shape, localInertia)
  val btBody = btRigidBody(rbInfo)
  btBody.activationState = CollisionConstants.DISABLE_DEACTIVATION
  btBody.friction = dynamicBody.friction
  return btBody
}

fun createCollisionShape(source: Shape): btCollisionShape {
  return when {

    source is ShapeOffset -> {
      val shape = btCompoundShape()
      shape.addChildShape(toGdxMatrix4(source.transform), createCollisionShape(source.shape))
      shape
    }

    source is Sphere -> btSphereShape(source.radius)

    source is Capsule -> btCapsuleShapeZ(source.radius, source.height)

    else -> throw Error("Not supported")
  }
}

fun createStaticFaceBody(face: ImmutableFace): btCollisionObject {
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

fun createStaticBody(body: Body, shape: btCollisionShape): btCollisionObject {
  val btBody = btCollisionObject()
  btBody.collisionShape = shape
  btBody.worldTransform = toGdxMatrix4(Matrix().translate(body.position).rotate(body.orientation))
  return btBody
}

fun syncMapGeometryAndPhysics(world: World, bulletState: BulletState) {
  val newFaceBodies = world.realm.faces
      .filterValues { it.faceType != FaceType.space && it.texture != null }
      .map { entry ->
        val face = world.realm.mesh.faces[entry.key]!!
        val collisionObject = createStaticFaceBody(face)
        bulletState.dynamicsWorld.addCollisionObject(collisionObject)
        // Not currently tracking static meshe mapping
        Pair(entry.key, collisionObject)
      }
//    bulletState.dynamicBodies = bulletState.dynamicBodies.plus(newFaceBodies)
}

fun syncNewBodies(world: World, bulletState: BulletState) {
  val deck = world.deck

  val newDynamicBodies = deck.dynamicBodies
      .filterKeys { key ->
        !bulletState.dynamicBodies.containsKey(key) && deck.collisionShapes.contains(key)
      }
      .map { (key, dynamicBody) ->
        val body = deck.bodies[key]!!
        val shape = createCollisionShape(deck.collisionShapes[key]!!)
        val btBody = createBulletDynamicObject(body, dynamicBody, shape)
        bulletState.dynamicsWorld.addRigidBody(btBody)
        Pair(key, btBody)
      }

  val newStaticBodies = deck.collisionShapes
      .filterKeys { key ->
        !deck.dynamicBodies.containsKey(key) && !bulletState.staticBodies.containsKey(key)
      }
      .map { (key, shapeDefinition) ->
        val shape = createCollisionShape(shapeDefinition)
        val body = deck.bodies[key]!!
        val collisionObject = createStaticBody(body, shape)
        bulletState.dynamicsWorld.addCollisionObject(collisionObject)
        Pair(key, collisionObject)
      }

  bulletState.dynamicBodies = bulletState.dynamicBodies
      .plus(newDynamicBodies)

  bulletState.staticBodies = bulletState.staticBodies.plus(newStaticBodies)

  if (!bulletState.isMapSynced) {
    bulletState.isMapSynced = true
    syncMapGeometryAndPhysics(world, bulletState)
  }
}