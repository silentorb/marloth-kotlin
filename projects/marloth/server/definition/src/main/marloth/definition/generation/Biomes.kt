package marloth.definition.generation

import generation.misc.*
import scenery.enums.TextureId

enum class BiomeId {
    checkers,
    exit,
    forest,
    home
}

val biomeInfoMap: BiomeInfoMap = mapOf(
    BiomeId.checkers to commonBiomeTemplate.copy(
        name = "checkers",
        floorTexture = TextureId.checkersBlackWhite,
        ceilingTexture = TextureId.checkersBlackWhite,
        wallTexture = TextureId.checkersBlackWhite
    ),
    BiomeId.exit to commonBiomeTemplate.copy(
        name = "exit",
        floorTexture = TextureId.algae,
        ceilingTexture = TextureId.algae,
        wallTexture = TextureId.algae,
        attributes = setOf(
            BiomeAttribute.placeOnlyAtEnd
        )
    ),
    BiomeId.forest to commonBiomeTemplate.copy(
        name = "forest",
        floorTexture = TextureId.grass,
        ceilingTexture = TextureId.bricks,
        wallTexture = TextureId.bricks
    ),
    BiomeId.home to commonBiomeTemplate.copy(
        name = "home",
        floorTexture = TextureId.redTile,
        ceilingTexture = TextureId.redTile,
        wallTexture = TextureId.redTile,
        wallPlacement = WallPlacement.all,
        attributes = setOf(
            BiomeAttribute.alwaysWindow,
            BiomeAttribute.alwaysLit,
            BiomeAttribute.placeOnlyAtStart
        )
    )
)
    .mapKeys { it.key.toString()}
