package marloth.definition.generation

import generation.misc.*
import scenery.MeshName
import scenery.enums.MeshId
import scenery.enums.TextureId

enum class BiomeId {
  checkers,
  exit,
  forest,
  home
}

//val commonBiomeTemplate = BiomeInfo("commonBiomeTemplate",
//    wallPlacement = WallPlacement.some,
//    roomFloorMeshes = listOf(MeshId.circleFloor),
//    roomFloorMeshesTall = listOf( MeshId.threeStoryCircleFloor),
//    tunnelFloorMeshes = listOf(MeshId.longStep),
//    stairStepMeshes = listOf(MeshId.longStairStep),
//    wallMeshes = listOf(MeshId.squareWall),
//    windowMeshes = listOf(MeshId.windowWall)
//)

val commonMeshes = setOf(
    MeshId.circleFloor,
    MeshId.threeStoryCircleFloor,
    MeshId.longStep,
    MeshId.longStairStep,
    MeshId.squareWall,
    MeshId.windowWall
)

fun mapMeshes(meshes: Set<MeshId>): Set<MeshName> = meshes.map { it.name }.toSet()

val biomeInfoMap: BiomeInfoMap = mapOf(
    BiomeId.checkers to BiomeInfo(
        name = "checkers",
        floorTexture = TextureId.checkersBlackWhite,
        ceilingTexture = TextureId.checkersBlackWhite,
        wallTexture = TextureId.checkersBlackWhite,
        meshes = mapMeshes(commonMeshes),
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.exit to BiomeInfo(
        name = "exit",
        floorTexture = TextureId.algae,
        ceilingTexture = TextureId.algae,
        wallTexture = TextureId.algae,
        meshes = mapMeshes(commonMeshes),
        attributes = setOf(
            BiomeAttribute.placeOnlyAtEnd,
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.forest to BiomeInfo(
        name = "forest",
        floorTexture = TextureId.grass,
        ceilingTexture = TextureId.bricks,
        wallTexture = TextureId.bricks,
        meshes = mapMeshes(commonMeshes),
        attributes = setOf(
            BiomeAttribute.wallsAll
        )
    ),
    BiomeId.home to BiomeInfo(
        name = "home",
        floorTexture = TextureId.redTile,
        ceilingTexture = TextureId.redTile,
        wallTexture = TextureId.redTile,
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
