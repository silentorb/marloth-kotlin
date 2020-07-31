package generation.architecture.engine

import marloth.scenery.enums.MeshInfoMap
import simulation.misc.Definitions

data class GenerationConfig(
    val definitions: Definitions,
    val meshes: MeshInfoMap,
    val includeEnemies: Boolean,
    val roomCount: Int
)
