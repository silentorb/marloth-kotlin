package generation.architecture.misc

import silentorb.mythic.ent.pipe
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.MeshName

val emptySet: MeshAttributes = setOf()

data class MeshQuery(
    val all: MeshAttributes = emptySet,
    val any: MeshAttributes = emptySet,
    val none: MeshAttributes = emptySet
) {
  operator fun plus(value: MeshQuery) =
      MeshQuery(
          all = all.plus(value.all),
          any = any.plus(value.any),
          none = none.plus(value.none)
      )
}

typealias MeshSource = (MeshQuery) -> Set<MeshName>

typealias MeshSelector = (MeshQuery) -> MeshName?

fun meshMatches(query: MeshQuery): (MeshAttributes) -> Boolean = { attributes ->
  val hasAny = if (query.any.any())
    query.any.any { attributes.contains(it) }
  else
    true

  val hasAll = if (query.all.any())
    query.all.all { attributes.contains(it) }
  else
    true

  val hasNone = if (query.none.any())
    query.none.none { attributes.contains(it) }
  else
    true

  hasAll && hasAny && hasNone
}

fun newMeshSource(meshInfo: MeshInfoMap): MeshSource {
  return { query ->
    assert(query.all.size + query.any.size + query.none.size > 0)
    pipe(
        { meshes: MeshInfoMap ->
          if (query.any.any())
            meshes.filterValues { value -> query.any.any { value.attributes.contains(it) } }
          else
            meshes
        },
        { meshes: MeshInfoMap ->
          if (query.all.any())
            meshes.filterValues { value -> query.all.all { value.attributes.contains(it) } }
          else
            meshes
        },
        { meshes: MeshInfoMap ->
          if (query.none.any())
            meshes.filterValues { value -> query.none.none { value.attributes.contains(it) } }
          else
            meshes
        }
    )(meshInfo)
        .keys
  }
}

fun randomlySelectMesh(dice: Dice, meshSource: MeshSource): MeshSelector = { query ->
  val meshPool = meshSource(query)
  if (meshPool.any())
    dice.takeOne(meshPool)
  else
    null
}

//fun filterMeshes(meshInfo: MeshInfoMap, options: Set<MeshName>, mode: QueryFilter = QueryFilter.all): (MeshAttributes) -> Set<MeshName> = { attributes ->
//  options.filter { key ->
//    val info = meshInfo[key]!!
//    when (mode) {
//      QueryFilter.all -> info.attributes.containsAll(attributes)
//      QueryFilter.none -> info.attributes.none { attributes.contains(it) }
//      QueryFilter.any -> info.attributes.any { attributes.contains(it) }
//    }
//  }.toSet()
//}

//fun filterMeshes(meshInfo: MeshInfoMap, biome: BiomeInfo, attributes: MeshAttributes, mode: QueryFilter = QueryFilter.all): Set<MeshName> =
//    filterMeshes(meshInfo, biome.meshes, mode)(attributes)
