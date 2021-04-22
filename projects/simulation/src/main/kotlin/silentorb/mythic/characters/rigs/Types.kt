package silentorb.mythic.characters.rigs

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
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
    val viewMode: ViewMode= ViewMode.firstPerson,
    val runSpeed: Float,
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
