package generation.misc

import scenery.enums.MeshId
import scenery.enums.TextureId

enum class BiomeId {
  checkers,
  exit,
  forest,
  home,
  void
}

enum class WallPlacement {
  all,
  none,
  some
}

data class BiomeInfo(
    val name: String,
    val wallPlacement: WallPlacement,
    val floorTexture: TextureId? = null,
    val ceilingTexture: TextureId? = null,
    val wallTexture: TextureId? = null,
    val roomFloorMeshes: List<MeshId>,
    val roomFloorMeshesTall: List<MeshId>,
    val tunnelFloorMeshes: List<MeshId>,
    val stairStepMeshes: List<MeshId>,
    val wallMeshes: List<MeshId>,
    val ceilingMeshes: List<MeshId> = listOf()
)

typealias BiomeInfoMap = Map<BiomeId, BiomeInfo>

val commonBiomeTemplate = BiomeInfo("commonBiomeTemplate",
    wallPlacement = WallPlacement.some,
    roomFloorMeshes = listOf(MeshId.circleFloor),
    roomFloorMeshesTall = listOf( MeshId.threeStoryCircleFloor),
    tunnelFloorMeshes = listOf(MeshId.longStep),
    stairStepMeshes = listOf(MeshId.longStairStep),
    wallMeshes = listOf(MeshId.squareWall)
)

val biomeInfoMap: BiomeInfoMap = mapOf(
    BiomeId.checkers to commonBiomeTemplate.copy(
        name = "checkers",
        floorTexture = TextureId.checkersBlackWhite,
        ceilingTexture = TextureId.checkersBlackWhite,
        wallTexture = TextureId.checkersBlackWhite
    ),
    BiomeId.exit to commonBiomeTemplate.copy(
        name = "checkers",
        floorTexture = TextureId.algae,
        ceilingTexture = TextureId.algae,
        wallTexture = TextureId.algae
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
        wallPlacement = WallPlacement.all
    )
)
