package generation.architecture.misc

import generation.architecture.engine.BlockBuilder
import silentorb.mythic.debugging.getDebugString
import simulation.misc.CellAttribute

fun getBlockBuilderFilter(source: String?): Set<CellAttribute>? =
    if (source != null)
      source
          .split(Regex("""\s*,\s"""))
          .map { token -> CellAttribute.values().first { it.name == token } }
          .toSet()
    else
      null

fun devFilterBlockBuilders(blockBuilders: Collection<BlockBuilder>): Collection<BlockBuilder> {
  val filter = getBlockBuilderFilter(getDebugString("BLOCK_ONLY"))
      ?.plus(setOf(CellAttribute.home))

  val exclude = getBlockBuilderFilter(getDebugString("BLOCK_EXCLUDE"))
      ?: setOf()

  val filtered = if (filter != null)
    blockBuilders.filter { it.block.attributes.intersect(filter).any() }
  else
    blockBuilders

  return filtered.filter { it.block.attributes.intersect(exclude).none() }
}
