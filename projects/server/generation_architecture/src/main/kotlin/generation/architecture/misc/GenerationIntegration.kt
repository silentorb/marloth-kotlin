package generation.architecture.misc

import generation.abstracted.*
import generation.architecture.definition.homeBlock
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
  val firstBlock = homeBlock

  val blockGrid = windingPath(dice, blockConfig, roomCount)(mapOf(
      Vector3i.zero to firstBlock.block
  ))

  val workbench = Workbench(
      blockGrid = blockGrid,
      mapGrid = mapGridFromBlockGrid(blockGrid)
  )
  return workbench
//  return pipe(
////      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..5)),
////      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..3)),
////      additionalConnecting(dice, blockConfig, rate = 0.7f)
//  )(workbench)
}
