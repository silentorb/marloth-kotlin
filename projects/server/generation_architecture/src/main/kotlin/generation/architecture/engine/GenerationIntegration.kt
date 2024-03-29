package generation.architecture.engine

import generation.architecture.biomes.Biomes
import generation.general.*
import silentorb.mythic.ent.reflectProperties
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i

fun newBlockGrid(seed: Long, dice: Dice, firstBlock: Block, blocks: Set<Block>, roomCount: Int): BlockGrid {
  val blockConfig = BlockConfig(
      blocks = blocks,
      biomes = reflectProperties<String>(Biomes).toSet()
  )
  return windingPath(seed, dice, blockConfig, roomCount, extractCells(firstBlock, Vector3i.zero))
}
