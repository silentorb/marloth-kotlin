package generation.architecture.misc

import generation.general.BiomeInfoMap
import marloth.scenery.enums.MeshInfoMap
import simulation.misc.Definitions

data class GenerationConfig(
    val definitions: Definitions,
    val biomes: BiomeInfoMap,
    val meshes: MeshInfoMap,
    val includeEnemies: Boolean,
    val roomCount: Int
)
