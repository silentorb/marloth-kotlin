package silentorb.mythic.characters.rigs

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3

enum class ViewMode {
  firstPerson,
  thirdPerson
}

fun characterRigOrentation(facingRotation: Vector2) =
    Quaternion()
        .rotateZ(facingRotation.x)
        .rotateY(-facingRotation.y)

data class CharacterRig(
    val facingRotation: Vector2 = Vector2(),
    val facingOrientation: Quaternion,
    val centerGroundDistance: Float = 0f,
    val groundDistance: Float = 0f,
    val firstPersonLookVelocity: Vector2 = Vector2(),
    val viewMode: ViewMode= ViewMode.firstPerson
) {
  val facingVector: Vector3
    get() = getFacingVector(facingOrientation)
}

data class ThirdPersonRig(
    val pivotLocation: Vector3,
    val rotation: Vector2,
    val orientationVelocity: Vector2 = Vector2.zero,
    val distance: Float,
    val facingDestination: Float?
)

data class CharacterRigMovement(
    val actor: Id,
    val offset: Vector3
)

object Freedom {
  const val none = 0
  const val walking = 1
  const val turning = 2
  const val orbiting = 4
  const val acting = 8

  const val all = -1
}

typealias Freedoms = Int

typealias FreedomTable = Table<Freedoms>

fun hasFreedom(freedoms: Freedoms, freedom: Freedoms): Boolean =
    freedoms and freedom != 0

fun hasFreedom(freedomTable: FreedomTable, actor: Id, freedom: Freedoms): Boolean =
    hasFreedom(freedomTable.getOrDefault(actor, Freedom.none), freedom)
