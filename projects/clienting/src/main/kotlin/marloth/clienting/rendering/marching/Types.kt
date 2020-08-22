package marloth.clienting.rendering.marching

import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.lookinglass.meshes.Primitive
import silentorb.mythic.scenery.SamplePoint
import silentorb.mythic.scenery.Shape
import silentorb.mythic.spatial.MutableVector3
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3i

//interface Ray {
//  val position: Vector3
//  val direction: Vector3
//}

data class MutableRay(
    var position: MutableVector3,
    var direction: MutableVector3
)

fun newMutableRay() =
    MutableRay(MutableVector3(), MutableVector3())

typealias RayCaster = (Vector2, MutableRay) -> Unit

data class CameraPerspective(
    val nearHalfWidth: Float,
    val farHalfWidth: Float
)

data class PointDistance(
    val depth: Float
)

data class MarchingConfig(
    val end: Float,
    val maxSteps: Int,
    val rayHitTolerance: Float = 0.001f
)

data class MarchingModelMesh(
    val vertices: List<SamplePoint>,
    val triangles: List<List<Int>>
)

typealias CellSourceMeshes = Map<Vector3i, MarchingModelMesh>

typealias CellGpuMeshes = Map<Vector3i, GeneralMesh>

data class MarchingGpuState(
    val meshes: CellGpuMeshes
)

fun newMarchingGpuState(): MarchingGpuState =
    MarchingGpuState(
        meshes = mapOf()
    )