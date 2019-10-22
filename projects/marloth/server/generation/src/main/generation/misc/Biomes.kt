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

enum class TextureGroup {
  ceiling,
  default,
  floor,
  wall
}

data class BiomeInfo(
    val name: String,
    val textures: Map<TextureGroup, TextureId>,
    val meshes: Set<MeshName>,
    val attributes: Set<BiomeAttribute> = setOf()
)

typealias BiomeInfoMap = Map<BiomeName, BiomeInfo>

enum class QueryFilter {
  all,
  any,
  none
}

fun biomeTexture(info: BiomeInfo, group: TextureGroup): TextureId? =
    info.textures[group] ?: info.textures[TextureGroup.default]

fun filterMeshes(meshInfo: MeshInfoMap, options: Set<MeshName>, mode: QueryFilter = QueryFilter.all):(MeshAttributes) -> Set<MeshName> = { attributes ->
  options.filter { key ->
    val info = meshInfo[key]!!
    when (mode) {
      QueryFilter.all -> info.attributes.containsAll(attributes)
      QueryFilter.none -> info.attributes.none { attributes.contains(it) }
      QueryFilter.any -> info.attributes.any { attributes.contains(it) }
    }
  }.toSet()
}

fun filterMeshes(meshInfo: MeshInfoMap, biome: BiomeInfo, attributes: MeshAttributes, mode: QueryFilter = QueryFilter.all): Set<MeshName> =
    filterMeshes(meshInfo, biome.meshes, mode)(attributes)
