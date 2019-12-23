package generation.architecture.misc

import generation.abstracted.HorrorVacuiConfig
import generation.abstracted.horrorVacui
import generation.abstracted.newWindingWorkbench
import generation.abstracted.windingPath
import generation.architecture.definition.BlockDefinitions
import generation.general.Block
import generation.general.BlockConfig
import generation.general.Workbench
import silentorb.mythic.ent.pipe
import silentorb.mythic.randomly.Dice

fun newWorkbench(dice: Dice, blocks: Set<Block>, independentConnections: Set<Any>, openConnectionTypes: Set<Any>,
                 roomCount: Int): Workbench {
  val blockConfig = BlockConfig(
      blocks = blocks,
      independentConnections = independentConnections,
      openConnections = openConnectionTypes
  )
  val firstBlockVariable = System.getenv("FIRST_BLOCK")
  val firstBlock = if (firstBlockVariable != null)
    getMember(BlockDefinitions, firstBlockVariable as String)
  else
    BlockDefinitions.singleCellDoorwayRoom

  return pipe(
      windingPath(dice, blockConfig, roomCount),
      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..2))
  )(newWindingWorkbench(firstBlock.block))
}
