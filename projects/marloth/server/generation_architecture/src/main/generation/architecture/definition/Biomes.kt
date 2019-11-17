package generation.architecture.definition

import generation.architecture.misc.*
import scenery.MeshName
import scenery.enums.MeshId
import scenery.enums.TextureId

enum class BiomeId {
  checkers,
  exit,
  forest,
  home
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

fun mapMeshes(meshes: Set<MeshId>): Set<MeshName> = meshes.map { it.name }.toSet()

val biomeInfoMap: BiomeInfoMap = mapOf(
    BiomeId.checkers to BiomeInfo(
        name = "checkers",
        textures = mapOf(
            TextureGroup.default to TextureId.checkersBlackWhite
        ),
        meshes = mapMeshes(commonMeshes),
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.exit to BiomeInfo(
        name = "exit",
        textures = mapOf(
            TextureGroup.default to TextureId.algae
        ),
        meshes = mapMeshes(commonMeshes),
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
        meshes = mapMeshes(commonMeshes),
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.home to BiomeInfo(
        name = "home",
        textures = mapOf(
            TextureGroup.default to TextureId.redTile
        ),
        meshes = mapMeshes(commonMeshes),
        attributes = setOf(
            BiomeAttribute.alwaysWindow,
            BiomeAttribute.alwaysLit,
            BiomeAttribute.placeOnlyAtStart,
            BiomeAttribute.wallsAll
        )
    )
)
    .mapKeys { it.key.toString() }
