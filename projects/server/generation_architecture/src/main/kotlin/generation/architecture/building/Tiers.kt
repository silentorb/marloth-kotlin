package generation.architecture.building

import generation.architecture.matrical.mergeBuilders
import generation.architecture.matrical.getLevelHeight
import marloth.scenery.enums.MeshId
import silentorb.mythic.spatial.Vector3

fun tieredWalls(level: Int) =
    cubeWallsWithFeatures(listOf(WallFeature.lamp, WallFeature.none), lampOffset = Vector3(0f, 0f, getLevelHeight(level) - 1.2f))

fun slopeBuilder(lower: Int) = mergeBuilders(
    newSlopedFloorMesh(MeshId.quarterSlope, getLevelHeight(lower)),
    tieredWalls(lower)
)
