package marloth.integration.scenery

import marloth.clienting.rendering.defaultAngle
import silentorb.mythic.scenery.Camera
import silentorb.mythic.scenery.ProjectionType
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3

data class FlyThroughCameraState(
    val location: Vector3,
    val orientation: Quaternion
)

private var flyThroughCameraState: FlyThroughCameraState? = null

fun getFlyThroughCameraState(): FlyThroughCameraState {
  if (flyThroughCameraState == null) {
    flyThroughCameraState = FlyThroughCameraState(
        location = Vector3.zero,
        orientation = Quaternion.zero
    )
  }

  return flyThroughCameraState!!
}

fun setFlyThroughCameraState(value: FlyThroughCameraState) {
  flyThroughCameraState = value
}

fun newFlyThroughCamera(location: Vector3, orientation: Quaternion): Camera =
    Camera(
        ProjectionType.perspective,
        location,
        orientation,
        defaultAngle
    )

fun newFlyThroughCamera(): Camera {
  val (location, orientation) = getFlyThroughCameraState()
  return newFlyThroughCamera(location, orientation)
}
