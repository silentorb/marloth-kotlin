package generation.architecture.misc

import generation.architecture.building.BlockBuilder
import silentorb.mythic.debugging.getDebugString
import simulation.misc.CellAttribute

fun getBlockBuilderFilter(source: String?): Set<CellAttribute>? =
    if (source != null)
      source
          .split(Regex("""\s*,\s"""))
          .map { token -> CellAttribute.values().first { it.name == token } }
          .toSet()
          .plus(setOf(CellAttribute.home))
    else
      null

fun devFilterBlockBuilders(blockBuilders: Collection<BlockBuilder>): Collection<BlockBuilder> {
  val filter = getBlockBuilderFilter(getDebugString("BLOCK_FILTER"))

  return if (filter != null)
    blockBuilders.filter { it.block.attributes.intersect(filter).any() }
  else
    blockBuilders
}
