package simulation

import scenery.Textures

enum class Biome {
  checkers,
  forest,
  home,
  void
}

data class BiomeInfo(
    val name: String,
    val enclosureRate: Float,
    val floorTexture: Textures,
    val ceilingTexture: Textures? = null,
    val wallTexture: Textures? = null
)

typealias BiomeInfoMap = Map<Biome, BiomeInfo>

val biomeInfoMap: BiomeInfoMap = mapOf(
    Biome.checkers to BiomeInfo("checkers",
        enclosureRate = 0.5f,
        floorTexture = Textures.cobblestone,
        ceilingTexture = Textures.checkers,
        wallTexture = Textures.swirl
    ),
    Biome.forest to BiomeInfo("forest",
        enclosureRate = 0.25f,
        floorTexture = Textures.grass,
        ceilingTexture = Textures.leaves,
        wallTexture = Textures.leaves
    ),
    Biome.home to BiomeInfo("home",
        enclosureRate = 1f,
        floorTexture = Textures.red_tile02,
        ceilingTexture = Textures.red_tile01,
        wallTexture = Textures.red_tile01
    ),
    Biome.void to BiomeInfo("void",
        floorTexture = Textures.none,
        enclosureRate = 0f)
)

val randomBiomes = listOf(
    Biome.checkers,
    Biome.forest
)