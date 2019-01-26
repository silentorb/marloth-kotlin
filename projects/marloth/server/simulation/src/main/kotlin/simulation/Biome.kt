package simulation

import scenery.Textures

enum class Biome {
  checkers,
  exit,
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
        wallTexture = Textures.darkCheckers
    ),
    Biome.exit to BiomeInfo("exit",
        enclosureRate = 0f,
        floorTexture = Textures.swirl,
        ceilingTexture = Textures.swirl,
        wallTexture = Textures.swirl
    ),
    Biome.forest to BiomeInfo("forest",
        enclosureRate = 0.25f,
        floorTexture = Textures.dirt,
        ceilingTexture = Textures.dirt,
        wallTexture = Textures.dirt
    ),
    Biome.home to BiomeInfo("home",
        enclosureRate = 1f,
        floorTexture = Textures.redTile02,
        ceilingTexture = Textures.redTile01,
        wallTexture = Textures.redTile01
    ),
    Biome.void to BiomeInfo("void",
        floorTexture = Textures.none,
        enclosureRate = 0f)
)
