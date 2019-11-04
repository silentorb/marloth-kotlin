package generation.misc

import simulation.misc.CellBiomeMap

data class GenerationConfig(
    val biomes: BiomeInfoMap,
    val meshes: MeshInfoMap,
    val includeEnemies: Boolean,
    val roomCount: Int
)
