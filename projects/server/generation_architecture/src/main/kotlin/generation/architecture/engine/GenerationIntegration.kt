package generation.architecture.engine

import generation.abstracted.*
import generation.general.Block
import generation.general.BlockConfig
import generation.general.Workbench
import generation.general.mapGridFromBlockGrid
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i

fun newWorkbench(dice: Dice, firstBlock: Block, blocks: Set<Block>, roomCount: Int): Workbench {
  val blockConfig = BlockConfig(
      blocks = blocks
  )
  val blockGrid = windingPath(dice, blockConfig, roomCount)(mapOf(
      Vector3i.zero to firstBlock
  ))

  return Workbench(
      blockGrid = blockGrid,
      mapGrid = mapGridFromBlockGrid(blockGrid)
  )
}
