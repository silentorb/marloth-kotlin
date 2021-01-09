package silentorb.mythic.characters.rigs

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld
import marloth.scenery.enums.CharacterRigCommands
import silentorb.mythic.cameraman.defaultLookMomentumAxis
import silentorb.mythic.cameraman.updateFirstPersonFacingRotation
import silentorb.mythic.cameraman.updateLookVelocityFirstPerson
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.firstFloatSortedBy
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.*
import silentorb.mythic.spatial.*
import simulation.main.Deck
import kotlin.math.min

const val defaultCharacterRadius = 0.16f
const val defaultCharacterHeight = 1.2f
const val characterGroundBuffer = 0.01f

const val groundedLinearDamping = 0.9f
const val airLinearDamping = 0f
const val airControlReduction = 0.4f

const val maxFootStepHeight = 0.3f

fun maxPositiveLookVelocityXChange() = 0.06f
fun maxNegativeLookVelocityXChange() = 0.15f

fun maxPositiveLookVelocityYChange() = 0.04f
fun maxNegativeLookVelocityYChange() = 0.08f

fun maxMoveVelocityChange() = 1f

fun lookSensitivity() = Vector2(.7f, .7f)

fun getFacingVector(orientation: Quaternion) =
    orientation * Vector3(1f, 0f, 0f)

data class AbsoluteOrientationForce(
    val body: Id,
    val orientation: Quaternion
)

fun footOffsets(radius: Float): List<Vector3> =
    createArcZ(radius + 0.9f, 8)

private const val noHitValue = 1f

private fun castFootStepRay(walkableMask: Int, dynamicsWorld: btDiscreteDynamicsWorld, bodyPosition: Vector3, footHeight: Float,
                            shapeHeight: Float): (Vector3) -> Float? {
  val basePosition = bodyPosition + Vector3(0f, 0f, -shapeHeight / 2f + footHeight)
  val rayLength = footHeight * 2f
  val endOffset = Vector3(0f, 0f, -rayLength)
  return { it: Vector3 ->
    val start = basePosition + it
    val end = start + endOffset
    null
    val result = firstRayHit(dynamicsWorld, start, end, walkableMask)
    if (result != null)
      start.z - result.hitPoint.z
    else
      null
  }
}

data class CharacterRigHand(
    val characterRig: CharacterRig,
    val body: Body,
    val collisionObject: CollisionObject
)

fun updateCharacterStepHeight(
    bulletState: BulletState,
    walkableMask: Int,
    body: Body,
    collisionObject: CollisionObject
): Float {
  val shape = collisionObject.shape
  val radius = shape.radius
  val footHeight = maxFootStepHeight
  val offsets = footOffsets(radius).plus(Vector3.zero)
  val cast = castFootStepRay(walkableMask, bulletState.dynamicsWorld, body.position, footHeight, shape.height)
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

fun isGrounded(characterRig: CharacterRig) =
    characterRig.groundDistance <= characterGroundBuffer

fun updateCharacterRigBulletBody(bulletState: BulletState): (Id, CharacterRig) -> Unit = { id, characterRig ->
  val groundDistance = characterRig.groundDistance
  val btBody = bulletState.dynamicBodies[id]!!
  val isGrounded = isGrounded(characterRig)
  if (isGrounded && (groundDistance < -0.01f || groundDistance > 0.1f)) {
    val stepHeight = -groundDistance

    if (stepHeight < 0f) {
      val k = 0
    }
    val transitionStepHeight = min(0.015f, stepHeight)
//    println("$groundDistance $transitionStepHeight")
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

fun updateCharacterRigBulletBodies(bulletState: BulletState, characterRigs: Table<CharacterRig>) {
  characterRigs.forEach(updateCharacterRigBulletBody(bulletState))
}

fun characterOrientationZ(characterRig: CharacterRig) =
    Quaternion().rotateZ(characterRig.facingRotation.x)

fun hoverCameraOrientationZ(thirdPersonRig: ThirdPersonRig) =
    Quaternion().rotateZ(thirdPersonRig.rotation.x - Pi / 2)

fun interpolateCharacterRigs(scalar: Float, first: Table<CharacterRig>, second: Table<CharacterRig>) =
    interpolateTables(scalar, first, second) { s, a, b ->
      a.copy(
          facingRotation = interpolate(s, a.facingRotation, b.facingRotation),
          facingOrientation = Quaternion(a.facingOrientation).nlerp(b.facingOrientation, s)
      )
    }

fun updateCharacterRig(
    bulletState: BulletState,
    walkableMask: Int,
    deck: Deck,
    freedomTable: Table<Freedoms>,
    events: Events,
    delta: Float
): (Id, CharacterRig) -> CharacterRig {
  val bodies = deck.bodies
  val collisionObjects = deck.collisionObjects
  val thirdPersonRigs = deck.thirdPersonRigs
  val allCommands = events.filterIsInstance<Command>()
  val mouseLookEvents = events.filterIsInstance<MouseLookEvent>()

  return { id, characterRig ->
    val commands = allCommands.filter { it.target == id }
    val freedoms = freedomTable[id] ?: Freedom.none
    val isFirstPerson = characterRig.viewMode == ViewMode.firstPerson

    val mouseLookEvent = mouseLookEvents.firstOrNull { it.character == id }

    val firstPersonLookVelocity = if (hasFreedom(freedoms, Freedom.turning) && isFirstPerson && mouseLookEvent == null) {
      val momentumAxis = if (deck.spirits.containsKey(id))
        spiritGamepadMomentumAxis()
      else
        defaultLookMomentumAxis()
      updateLookVelocityFirstPerson(commands, momentumAxis, characterRig.firstPersonLookVelocity)
    } else
      characterRig.firstPersonLookVelocity

    val facingRotation = if (!hasFreedom(freedoms, Freedom.turning))
      characterRig.facingRotation
    else if (isFirstPerson)
      updateFirstPersonFacingRotation(characterRig.facingRotation, mouseLookEvent?.offset, firstPersonLookVelocity, delta)
    else
      updateThirdPersonFacingRotation(characterRig.facingRotation, thirdPersonRigs[id]!!, delta)

    val viewMode = if (commands.any { it.type == CharacterRigCommands.switchView })
      if (isFirstPerson) ViewMode.thirdPerson else ViewMode.firstPerson
    else
      characterRig.viewMode

//    assert(facingRotation.z > - Pi * 2 && facingRotation.z < Pi * 2)
    characterRig.copy(
        groundDistance = updateCharacterStepHeight(bulletState, walkableMask, bodies[id]!!, collisionObjects[id]!!),
        facingRotation = facingRotation,
        facingOrientation = characterRigOrentation(facingRotation),
        firstPersonLookVelocity = firstPersonLookVelocity,
        viewMode = viewMode
    )
  }
}
