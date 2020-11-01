package marloth.clienting.rendering.marching

import silentorb.mythic.glowing.GeneralMesh
import silentorb.mythic.spatial.MutableVector3
import silentorb.mythic.spatial.Vector2
import silentorb.mythic.spatial.Vector3i

const val marchingRenderLayer: String = "marchingLayer"

const val renderCellsService: String = "renderCellsService"

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

typealias CellTimingMap = Map<Vector3i, Long>

data class MarchingGpuState(
    val meshes: CellGpuMeshes
)

data class MarchingState(
    val pendingCells: Set<Vector3i>,
    val gpu: MarchingGpuState,
    val timeMeasurements: ServiceTimes,
    val lastUsed: CellTimingMap
)

fun newMarchingGpuState(): MarchingGpuState =
    MarchingGpuState(
        meshes = mapOf()
    )

fun newMarchingState(): MarchingState =
    MarchingState(
        pendingCells = setOf(),
        gpu = newMarchingGpuState(),
        timeMeasurements = mapOf(),
        lastUsed = mapOf()
    )
