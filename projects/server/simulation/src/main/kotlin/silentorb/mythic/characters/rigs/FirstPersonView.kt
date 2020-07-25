package silentorb.mythic.characters.rigs

import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.minMax
import silentorb.mythic.spatial.normalizeRadialAngle

fun updateFirstPersonFacingRotation(facingRotation: Vector2, mouseLookOffset: Vector2?, lookVelocity: Vector2, delta: Float): Vector2 {
  val next = if (mouseLookOffset != null)
    facingRotation + mouseLookOffset * Vector2(2f, 1.3f)
  else
    facingRotation + fpCameraRotation(lookVelocity, delta)

  return Vector2(
      normalizeRadialAngle(next.x),
      minMax(next.y, -1.1f, 1.1f)
  )
}