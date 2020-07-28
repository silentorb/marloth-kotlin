package generation.architecture.building

import generation.architecture.matrical.mergeBuilders
import generation.architecture.matrical.Level
import generation.architecture.matrical.tieredWalls
import marloth.scenery.enums.MeshId

fun slopeBuilder(lower: Level) = mergeBuilders(
    newSlopedFloorMesh(MeshId.quarterSlope, lower.height),
    tieredWalls(lower.index)
)
