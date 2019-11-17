package generation.architecture.misc

import scenery.MeshName

enum class QueryFilter {
  all,
  any,
  none
}

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
