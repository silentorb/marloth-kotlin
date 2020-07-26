package generation.architecture.misc

import generation.abstracted.*
import generation.architecture.definition.homeBlock1
import generation.general.Block
import generation.general.BlockConfig
import generation.general.Workbench
import generation.general.mapGridFromBlockGrid
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i

fun newWorkbench(dice: Dice, blocks: Set<Block>, roomCount: Int): Workbench {
  val blockConfig = BlockConfig(
      blocks = blocks
  )
  val firstBlock = homeBlock1

  val blockGrid = windingPath(dice, blockConfig, roomCount)(mapOf(
      Vector3i.zero to firstBlock.block
  ))

  return Workbench(
      blockGrid = blockGrid,
      mapGrid = mapGridFromBlockGrid(blockGrid)
  )
}
