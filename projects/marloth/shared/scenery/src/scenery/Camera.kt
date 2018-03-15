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
    val angleOrZoom: Float,
    val nearClip: Float = 0.01f,
    val farClip: Float = 200f)