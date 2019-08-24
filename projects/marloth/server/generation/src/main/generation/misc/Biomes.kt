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

enum class QueryMode {
  all,
  any,
  none
}

fun queryMeshes(meshInfo: MeshInfoMap, options: Set<MeshName>, attributes: MeshAttributes, mode: QueryMode = QueryMode.all): Set<MeshName> =
    options.filter { key ->
      val info = meshInfo[key]!!
      when (mode) {
        QueryMode.all -> info.attributes.containsAll(attributes)
        QueryMode.none -> info.attributes.none { attributes.contains(it) }
        QueryMode.any -> info.attributes.any { attributes.contains(it) }
      }
      info.attributes.containsAll(attributes)
    }.toSet()

fun queryMeshes(meshInfo: MeshInfoMap, biome: BiomeInfo, attributes: MeshAttributes, mode: QueryMode = QueryMode.all): Set<MeshName> =
    queryMeshes(meshInfo, biome.meshes, attributes, mode)
