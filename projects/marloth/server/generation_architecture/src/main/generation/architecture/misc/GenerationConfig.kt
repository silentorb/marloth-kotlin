package generation.architecture.misc

data class GenerationConfig(
    val biomes: BiomeInfoMap,
    val meshes: MeshInfoMap,
    val includeEnemies: Boolean,
    val roomCount: Int
)
