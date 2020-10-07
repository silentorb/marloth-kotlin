package silentorb.marloth.world_generation.imp

import silentorb.imp.core.PathKey
import silentorb.imp.core.newTypePair
import silentorb.imp.execution.typePairsToTypeNames

const val worldGenerationPath = "silentorb.marloth.generation.world"

val handType = newTypePair(PathKey(worldGenerationPath, "Hand"))
val spatialNodeType = newTypePair(PathKey(worldGenerationPath, "SpatialNode"))
val spatialNodeListType = newTypePair(PathKey(worldGenerationPath, "SpatialNodeList"))

fun worldGenerationTypes() =
    typePairsToTypeNames(
        listOf(
            handType,
            spatialNodeType,
        )
    )
