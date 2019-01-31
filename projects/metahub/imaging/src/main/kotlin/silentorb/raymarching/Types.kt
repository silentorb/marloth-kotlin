package silentorb.raymarching

import mythic.spatial.Vector3

data class Camera(
    val position: Vector3,
    val direction: Vector3
)

data class Ray(
    val position: Vector3,
    val direction: Vector3
)

typealias Normal = (Vector3) -> Vector3

data class PointDistance(
    val value: Float,
    val normal: Normal = zeroNormal
)

data class Marcher(
    val end: Float,
    val maxSteps: Int
)

data class Scene(
    val sdf: Sdf,
    val camera: Camera,
    val lights: List<Light>
)
