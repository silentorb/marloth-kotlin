package simulation.physics

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import org.joml.times
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.firstFloatSortedBy
import silentorb.mythic.physics.*
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.createArcZ
import simulation.entities.*
import simulation.input.Commands
import simulation.main.Deck
import simulation.misc.joinInputVector
import simulation.misc.playerMoveMap
import kotlin.math.min

const val defaultCharacterRadius = 0.3f
const val defaultCharacterHeight = 1.2f
const val characterGroundBuffer = 0.02f

const val maxFootStepHeight = 0.6f

data class CharacterRig(
    val facingRotation: Vector3 = Vector3(),
    val groundDistance: Float = 0f,
    val maxSpeed: Float
) {
  val facingQuaternion: Quaternion
    get() = Quaternion()
        .rotateZ(facingRotation.z)
        .rotateY(facingRotation.y)

  val facingVector: Vector3
    get() = facingQuaternion * Vector3(1f, 0f, 0f)
}

fun footOffsets(radius: Float): List<Vector3> =
    createArcZ(radius + 0.9f, 8)

private const val noHitValue = 1f

private fun castFootStepRay(dynamicsWorld: btDiscreteDynamicsWorld, bodyPosition: Vector3, footHeight: Float,
                            shapeHeight: Float): (Vector3) -> Float? {
  val basePosition = bodyPosition + Vector3(0f, 0f, -shapeHeight / 2f + footHeight)
  val rayLength = footHeight * 2f
  val endOffset = Vector3(0f, 0f, -rayLength)
  return { it: Vector3 ->
    val start = basePosition + it
    val end = start + endOffset
    castCollisionRay(dynamicsWorld, start, end)?.distance
  }
}

fun updateCharacterStepHeight(bulletState: BulletState, deck: Deck, character: Id): Float {
  val body = deck.bodies[character]!!
  val collisionObject = deck.collisionShapes[character]!!
  val shape = collisionObject.shape
  val radius = shape.radius
  val footHeight = maxFootStepHeight
  val offsets = footOffsets(radius).plus(Vector3.zero)
  val cast = castFootStepRay(bulletState.dynamicsWorld, body.position, footHeight, shape.height)
  val centerDistance = cast(Vector3.zero)
  if (centerDistance == null) {
    return noHitValue
  } else {
    val distances = offsets
        .mapNotNull(cast)
        .plus(centerDistance)

    return if (distances.any()) {
      val distance = distances.firstFloatSortedBy { it }
      distance - footHeight
    } else
      noHitValue
  }
}

fun isGrounded(characterRig: CharacterRig) = characterRig.groundDistance <= characterGroundBuffer

fun updateCharacterRig(bulletState: BulletState, deck: PhysicsDeck, id: Id) {
  val characterRig = deck.characterRigs[id]!!
  val groundDistance = characterRig.groundDistance
  val btBody = bulletState.dynamicBodies[id]!!
  val isGrounded = isGrounded(characterRig)
  if (isGrounded && (groundDistance < -0.01f || groundDistance > 0.1f)) {
    val stepHeight = -groundDistance

    val body = deck.bodies[id]!!
    if (stepHeight < 0f) {
      val k = 0
    }
    val transitionStepHeight = min(0.03f, stepHeight)
    btBody.translate(toGdxVector3(Vector3(0f, 0f, transitionStepHeight)))
  }

  val linearDamping = if (isGrounded)
    groundedLinearDamping
  else
    airLinearDamping

  val gravity = if (isGrounded)
    com.badlogic.gdx.math.Vector3.Zero
  else
    staticGravity()

  btBody.setDamping(linearDamping, 0f)
  btBody.gravity = gravity
}

fun updateCharacterRigs(bulletState: BulletState, deck: PhysicsDeck) {
  for ((id, _) in deck.characterRigs) {
    updateCharacterRig(bulletState, deck, id)
  }
}

fun characterOrientationZ(characterRig: CharacterRig) =
    Quaternion().rotateZ(characterRig.facingRotation.z - Pi / 2)

fun characterMovementFp(commands: Commands, characterRig: CharacterRig, id: Id, body: Body): LinearImpulse? {
  val offsetVector = joinInputVector(commands, playerMoveMap)
  return if (offsetVector != null) {
    val airControlMod = if (isGrounded(characterRig)) 1f else airControlReduction
    val direction = characterOrientationZ(characterRig) * offsetVector * airControlMod
    val baseSpeed = characterRig.maxSpeed
    val maxImpulseLength = baseSpeed
    val commandVector = direction * maxImpulseLength
    val horizontalVelocity = body.velocity.copy(z = 0f)
    val impulseVector = getMovementImpulseVector(baseSpeed, horizontalVelocity, commandVector)
    val finalImpulse = impulseVector * 5f
    LinearImpulse(body = id, offset = finalImpulse)
  } else {
    null
  }
}
