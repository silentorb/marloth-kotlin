package generation.misc

import scenery.enums.MeshId
import scenery.enums.TextureId
import simulation.misc.BiomeName

enum class WallPlacement {
  all,
  none,
  some
}

enum class BiomeAttribute {
  alwaysWindow,
  alwaysLit,
  placeOnlyAtStart,
  placeOnlyAtEnd
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
    val windowMeshes: List<MeshId>,
    val ceilingMeshes: List<MeshId> = listOf(),
    val attributes: Set<BiomeAttribute> = setOf()
)

typealias BiomeInfoMap = Map<BiomeName, BiomeInfo>

val commonBiomeTemplate = BiomeInfo("commonBiomeTemplate",
    wallPlacement = WallPlacement.some,
    roomFloorMeshes = listOf(MeshId.circleFloor),
    roomFloorMeshesTall = listOf( MeshId.threeStoryCircleFloor),
    tunnelFloorMeshes = listOf(MeshId.longStep),
    stairStepMeshes = listOf(MeshId.longStairStep),
    wallMeshes = listOf(MeshId.squareWall),
    windowMeshes = listOf(MeshId.windowWall)
)

val meshesThatCanHaveAttachments = setOf(
    MeshId.squareWall,
    MeshId.pillowWall
)

