package generation.architecture.engine

import generation.abstracted.*
import generation.general.*
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i

fun newBlockGrid(dice: Dice, firstBlock: Block, blocks: Set<Block>, roomCount: Int): BlockGrid {
  val blockConfig = BlockConfig(
      blocks = blocks
  )
  return windingPath(dice, blockConfig, roomCount)(mapOf(
      Vector3i.zero to firstBlock
  ))
}
