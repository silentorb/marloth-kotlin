package scenery

import mythic.spatial.Quaternion
import mythic.spatial.Vector3

enum class ProjectionType {
  orthographic,
  perspective
}

data class Camera(
    val projectionType: ProjectionType,
    val position: Vector3,
    val orientation: Quaternion,
    val angle: Float)