package generation.architecture.misc

import generation.abstracted.*
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
  val firstBlock = BlockDefinitions.home

  return pipe(
      windingPath(dice, blockConfig, roomCount),
      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..3)),
      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..3)),
      additionalConnecting(dice, blockConfig, rate = 0.7f)
  )(newWindingWorkbench(firstBlock.block))
}
