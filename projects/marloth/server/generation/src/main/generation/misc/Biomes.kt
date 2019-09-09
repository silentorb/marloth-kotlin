package generation.misc

import scenery.MeshName
import scenery.enums.TextureId
import simulation.misc.BiomeName

//enum class WallPlacement {
//  all,
//  none,
//  some
//}

enum class BiomeAttribute {
  alwaysWindow,
  alwaysLit,
  placeOnlyAtStart,
  placeOnlyAtEnd,
  wallsAll,
  wallsSome
}

data class BiomeInfo(
    val name: String,
    val floorTexture: TextureId? = null,
    val ceilingTexture: TextureId? = null,
    val wallTexture: TextureId? = null,
    val meshes: Set<MeshName>,
    val attributes: Set<BiomeAttribute> = setOf()
//    val roomFloorMeshes: List<MeshId>,
//    val roomFloorMeshesTall: List<MeshId>,
//    val tunnelFloorMeshes: List<MeshId>,
//    val stairStepMeshes: List<MeshId>,
//    val wallMeshes: List<MeshId>,
//    val windowMeshes: List<MeshId>,
//    val ceilingMeshes: List<MeshId> = listOf(),
)

typealias BiomeInfoMap = Map<BiomeName, BiomeInfo>

enum class QueryFilter {
  all,
  any,
  none
}

fun queryMeshes(meshInfo: MeshInfoMap, options: Set<MeshName>, attributes: MeshAttributes, mode: QueryFilter = QueryFilter.all): Set<MeshName> =
    options.filter { key ->
      val info = meshInfo[key]!!
      when (mode) {
        QueryFilter.all -> info.attributes.containsAll(attributes)
        QueryFilter.none -> info.attributes.none { attributes.contains(it) }
        QueryFilter.any -> info.attributes.any { attributes.contains(it) }
      }
    }.toSet()

fun queryMeshes(meshInfo: MeshInfoMap, biome: BiomeInfo, attributes: MeshAttributes, mode: QueryFilter = QueryFilter.all): Set<MeshName> =
    queryMeshes(meshInfo, biome.meshes, attributes, mode)
