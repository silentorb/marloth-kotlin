package simulation

import scenery.MeshId
import scenery.TextureId

enum class BiomeId {
  checkers,
  exit,
  forest,
  home,
  void
}

data class BiomeInfo(
    val name: String,
    val wallEnclosureRate: Float = 0.75f,
    val floorTexture: TextureId? = null,
    val ceilingTexture: TextureId? = null,
    val wallTexture: TextureId? = null,
    val roomFloorMeshes: List<MeshId>,
    val tunnelFloorMeshes: List<MeshId>,
    val wallMeshes: List<MeshId>,
    val ceilingMeshes: List<MeshId> = listOf()
)

typealias BiomeInfoMap = Map<BiomeId, BiomeInfo>

val biomeInfoMap: BiomeInfoMap = mapOf(
    BiomeId.checkers to BiomeInfo("checkers",
        wallEnclosureRate = 0.5f,
        floorTexture = TextureId.checkersBlackWhite,
        ceilingTexture = TextureId.checkersBlackWhite,
        wallTexture = TextureId.checkersBlackWhite,
        roomFloorMeshes = listOf(MeshId.circleFloor, MeshId.tallCircleFloor),
        tunnelFloorMeshes = listOf(MeshId.longStep),
        wallMeshes = listOf(MeshId.squareWall)
    ),
    BiomeId.exit to BiomeInfo("exit",
        wallEnclosureRate = 0f,
        floorTexture = TextureId.algae,
        ceilingTexture = TextureId.algae,
        wallTexture = TextureId.algae,
        roomFloorMeshes = listOf(MeshId.circleFloor, MeshId.tallCircleFloor),
        tunnelFloorMeshes = listOf(MeshId.longStep),
        wallMeshes = listOf(MeshId.squareWall)
    ),
    BiomeId.forest to BiomeInfo("forest",
        wallEnclosureRate = 0.25f,
        floorTexture = TextureId.grass,
        ceilingTexture = TextureId.bricks,
        wallTexture = TextureId.bricks,
        roomFloorMeshes = listOf(MeshId.circleFloor, MeshId.tallCircleFloor),
        tunnelFloorMeshes = listOf(MeshId.longStep),
        wallMeshes = listOf(MeshId.squareWall)
    ),
    BiomeId.home to BiomeInfo("home",
        wallEnclosureRate = 1f,
        floorTexture = TextureId.redTile,
        ceilingTexture = TextureId.redTile,
        wallTexture = TextureId.redTile,
        roomFloorMeshes = listOf(MeshId.circleFloor, MeshId.tallCircleFloor),
        tunnelFloorMeshes = listOf(MeshId.longStep),
        wallMeshes = listOf(MeshId.squareWall)
    ),
    BiomeId.void to BiomeInfo("void",
        floorTexture = TextureId.none,
        wallEnclosureRate = 0f,
        roomFloorMeshes = listOf(),
        tunnelFloorMeshes = listOf(MeshId.longStep),
        wallMeshes = listOf()
    )
)
