package silentorb.raymarching

import mythic.spatial.Quaternion
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import java.nio.FloatBuffer

data class Camera(
    val position: Vector3,
    val orientation: Quaternion,
    val near: Float,
    val far: Float
)

data class CameraPerspective(
    val nearHalfWidth: Float,
    val farHalfWidth: Float
)

typealias RayCaster = (Vector2) -> Ray

data class Ray(
    val position: Vector3,
    val direction: Vector3
)

data class MarchedPoint(
    val color: Vector3,
    val depth: Float,
    val position: Vector3,
    val normal: Vector3
)

typealias Normal = (SdfHook, Vector3) -> Vector3

data class Geometry(
    val normal: Normal
)

data class PointDistance(
    val value: Float,
    val geometry: Geometry? = null
)

val rayMiss = PointDistance(100000f)

data class Marcher(
    val end: Float,
    val maxSteps: Int
)

data class Scene(
    val sdf: RaySdf,
    val camera: Camera,
    val lights: List<Light>
)

data class MarchedBuffers(
    val color: FloatBuffer,
    val depth: FloatBuffer,
    val position: FloatBuffer,
    val normal: FloatBuffer
)