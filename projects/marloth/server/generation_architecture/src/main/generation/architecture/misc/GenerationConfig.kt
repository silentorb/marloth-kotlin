package generation.architecture.misc

import generation.general.BiomeInfoMap

data class GenerationConfig(
    val biomes: BiomeInfoMap,
    val meshes: MeshInfoMap,
    val includeEnemies: Boolean,
    val roomCount: Int
)
