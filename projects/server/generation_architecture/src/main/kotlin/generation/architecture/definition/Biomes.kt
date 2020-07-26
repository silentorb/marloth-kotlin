package generation.architecture.definition

import generation.general.BiomeAttribute
import generation.general.BiomeInfo
import generation.general.BiomeInfoMap
import generation.general.TextureGroup
import silentorb.mythic.scenery.MeshName
import marloth.scenery.enums.MeshId
import marloth.scenery.enums.TextureId

object BiomeId {
  const val checkers = "checkers"
  const val exit = "exit"
  const val forest = "forest"
  const val home = "home"
  const val tealPalace = "tealPalace"
  const val village = "village"
}

val commonMeshes = setOf(
    MeshId.circleFloor,
    MeshId.curvingStairStep,
    MeshId.fillerColumn,
    MeshId.halfCircleFloor,
    MeshId.longStep,
    MeshId.longStairStep,
    MeshId.squareWall
)

val biomeInfoMap: BiomeInfoMap = mapOf(
    BiomeId.checkers to BiomeInfo(
        name = "checkers",
        textures = mapOf(
            TextureGroup.default to TextureId.checkersBlackWhite
        ),
        meshes = commonMeshes,
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.exit to BiomeInfo(
        name = "exit",
        textures = mapOf(
            TextureGroup.default to TextureId.algae
        ),
        meshes = commonMeshes,
        attributes = setOf(
            BiomeAttribute.placeOnlyAtEnd,
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.forest to BiomeInfo(
        name = "forest",
        textures = mapOf(
            TextureGroup.default to TextureId.bricks,
            TextureGroup.floor to TextureId.grass
        ),
        meshes = commonMeshes,
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.home to BiomeInfo(
        name = "home",
        textures = mapOf(
            TextureGroup.default to TextureId.redTile
        ),
        meshes = commonMeshes,
        attributes = setOf(
            BiomeAttribute.alwaysWindow,
            BiomeAttribute.alwaysLit,
            BiomeAttribute.placeOnlyAtStart,
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.tealPalace to BiomeInfo(
        name = "tealPalace",
        textures = mapOf(
            TextureGroup.default to TextureId.diamondTiles,
            TextureGroup.wall to TextureId.arch
        ),
        meshes = commonMeshes,
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.village to BiomeInfo(
        name = "village",
        textures = mapOf(
            TextureGroup.floor to TextureId.cobblestone,
            TextureGroup.wall to TextureId.woodPanel
        ),
        meshes = commonMeshes,
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    )
)
    .mapKeys { it.key.toString() }
