package simulation

import scenery.TextureId

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
    val floorTexture: TextureId,
    val ceilingTexture: TextureId? = null,
    val wallTexture: TextureId? = null
)

typealias BiomeInfoMap = Map<Biome, BiomeInfo>

val biomeInfoMap: BiomeInfoMap = mapOf(
    Biome.checkers to BiomeInfo("checkers",
        enclosureRate = 0.5f,
        floorTexture = TextureId.cobblestone,
        ceilingTexture = TextureId.checkersBlackWhite,
        wallTexture = TextureId.darkCheckers
    ),
    Biome.exit to BiomeInfo("exit",
        enclosureRate = 0f,
        floorTexture = TextureId.algae,
        ceilingTexture = TextureId.algae,
        wallTexture = TextureId.algae
    ),
    Biome.forest to BiomeInfo("forest",
        enclosureRate = 0.25f,
        floorTexture = TextureId.dirt,
        ceilingTexture = TextureId.dirt,
        wallTexture = TextureId.dirt
    ),
    Biome.home to BiomeInfo("home",
        enclosureRate = 1f,
        floorTexture = TextureId.redTile,
        ceilingTexture = TextureId.redTile,
        wallTexture = TextureId.redTile
    ),
    Biome.void to BiomeInfo("void",
        floorTexture = TextureId.none,
        enclosureRate = 0f)
)
