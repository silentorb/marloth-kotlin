package silentorb.raymarching

import mythic.spatial.*
import org.joml.Vector3f
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

interface Ray {
  val position: Vector3int
  val direction: Vector3int
}

data class MutableRay(
    override var position: MutableVector3,
    override var direction: MutableVector3
) : Ray

typealias RayCaster = (Vector2, MutableRay) -> Unit

data class MarchedPoint(
    var color: MutableVector3 = MutableVector3(),
    var depth: Float = 0f,
    var position: MutableVector3 = MutableVector3(),
    var normal: MutableVector3= MutableVector3()
)

typealias Normal = (SdfHook, Vector3) -> Vector3

data class Geometry(
    val normal: Normal
)

//data class PointDistance(
//    val value: Float,
//    val geometry: Geometry? = null
//)

val rayMissValue = 100000f
//val rayMiss = PointDistance(100000f)

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