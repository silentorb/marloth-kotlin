package physics

import com.badlogic.gdx.physics.bullet.collision.*
import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState
import mythic.sculpting.ImmutableFace
import mythic.spatial.Matrix
import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.getCenter
import scenery.*
import simulation.FaceType
import simulation.World
import com.badlogic.gdx.math.Vector3 as GdxVector3

fun createCollisionShape(source: Shape, scale: Vector3): btCollisionShape {
  return when (source) {
    is ShapeOffset -> {
      val shape = btCompoundShape()
      shape.addChildShape(toGdxMatrix4(source.transform), createCollisionShape(source.shape, scale))
      shape
    }
    is Box -> btBoxShape(toGdxVector3(source.halfExtents * scale))
    is Sphere -> btSphereShape(source.radius * scale.x)
    is Capsule -> btCapsuleShapeZ(source.radius * scale.x, source.height * scale.z)
    is Cylinder -> btCylinderShapeZ(toGdxVector3(Vector3(source.radius * scale.x, source.radius * scale.y, source.height * scale.z * 0.5f)))
    else -> throw Error("Not supported")
  }
}

fun createBulletDynamicObject(body: Body, dynamicBody: DynamicBody, shape: btCollisionShape, rotationalInertia: Boolean): btRigidBody {
  val transform = Matrix().translate(body.position).rotate(body.orientation)
  val localInertia = com.badlogic.gdx.math.Vector3(0f, 0f, 0f)
  if (rotationalInertia)
    shape.calculateLocalInertia(dynamicBody.mass, localInertia)

  val myMotionState = btDefaultMotionState(toGdxMatrix4(transform))
  val rbInfo = btRigidBody.btRigidBodyConstructionInfo(dynamicBody.mass, myMotionState, shape, localInertia)
  val btBody = btRigidBody(rbInfo)
  btBody.activationState = CollisionConstants.DISABLE_DEACTIVATION
  btBody.friction = dynamicBody.friction
  return btBody
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

//fun syncMapGeometryAndPhysics(world: World, bulletState: BulletState) {
//  val newFaceBodies = world.realm.faces
//      .filterValues { it.faceType != FaceType.space && it.texture != null }
//      .map { entry ->
//        val face = world.realm.mesh.faces[entry.key]!!
//        val collisionObject = createStaticFaceBody(face)
//        bulletState.dynamicsWorld.addCollisionObject(collisionObject)
//        // Not currently tracking static mesh mapping
//        Pair(entry.key, collisionObject)
//      }
////    bulletState.dynamicBodies = bulletState.dynamicBodies.plus(newFaceBodies)
//}

fun syncNewBodies(world: World, bulletState: BulletState) {
  val deck = world.deck

  val newDynamicBodies = deck.dynamicBodies
      .filterKeys { key ->
        !bulletState.dynamicBodies.containsKey(key) && deck.collisionShapes.contains(key)
      }
      .map { (key, dynamicBody) ->
        val body = deck.bodies[key]!!
        val shape = createCollisionShape(deck.collisionShapes[key]!!, body.scale)
        val hingeInfo = dynamicBody.hinge
        val bulletBody = createBulletDynamicObject(body, dynamicBody, shape, hingeInfo != null)
        if (hingeInfo != null) {
          val hinge = btHingeConstraint(bulletBody, toGdxVector3(hingeInfo.pivot * body.scale), toGdxVector3(hingeInfo.axis), true)
//          hinge.enableAngularMotor(true, 1f, 1f)
//          hinge.setLimit(-Pi * 0.2f, Pi  * 0.2f)
          val offset = Pi / 2f
//          hinge.setLimit(-Pi * 0.2f - offset, Pi  * 0.2f - offset)
          hinge.setLimit(-Pi, Pi)
          val j = hinge.hingeAngle

          bulletState.dynamicsWorld.addConstraint(hinge)
          hinge.setDbgDrawSize(5f)
//          bulletBody.setAngularVelocity(GdxVector3(0f, 0f, 1f))

        }
        bulletState.dynamicsWorld.addRigidBody(bulletBody)
        Pair(key, bulletBody)
      }

  val newStaticBodies = deck.collisionShapes
      .filterKeys { key ->
        !deck.dynamicBodies.containsKey(key) && !bulletState.staticBodies.containsKey(key)
      }
      .map { (key, shapeDefinition) ->
        val body = deck.bodies[key]!!
        val shape = createCollisionShape(shapeDefinition, body.scale)
        val collisionObject = createStaticBody(body, shape)
        bulletState.dynamicsWorld.addCollisionObject(collisionObject)
        Pair(key, collisionObject)
      }

  bulletState.dynamicBodies = bulletState.dynamicBodies
      .plus(newDynamicBodies)

  bulletState.staticBodies = bulletState.staticBodies.plus(newStaticBodies)

  if (!bulletState.isMapSynced) {
    bulletState.isMapSynced = true
//    syncMapGeometryAndPhysics(world, bulletState)
  }
}
