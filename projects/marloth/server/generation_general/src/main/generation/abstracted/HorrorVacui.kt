package generation.abstracted

import generation.general.BlockConfig
import generation.general.Workbench
import generation.general.blockCanHaveMoreConnections
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice

data class HorrorVacuiConfig(
    val branchRate: Float,
    val branchLengthRange: IntRange
)

private fun horrorVacuiBranch(dice: Dice, blockConfig: BlockConfig, workbench: Workbench,
                              branchLengthRange: IntRange, origins: List<Vector3i>): Workbench {
  return if (origins.any()) {
    val branchLength = dice.getInt(branchLengthRange)
    val nextWorkbench = addPathStep(branchLength, dice, blockConfig, workbench, origins.first())
    horrorVacuiBranch(dice, blockConfig, nextWorkbench, branchLengthRange, origins.drop(1))
  } else
    workbench
}

fun horrorVacui(dice: Dice, blockConfig: BlockConfig,
                config: HorrorVacuiConfig): (Workbench) -> Workbench = { workbench ->
  val blockGrid = workbench.blockGrid
  val branchCount = (blockGrid.size.toFloat() * config.branchRate).toInt()
  val possibleCells = workbench.blockGrid.keys.filter(blockCanHaveMoreConnections(blockConfig, blockGrid))
  val origins = dice.shuffle(possibleCells).take(branchCount)
  horrorVacuiBranch(dice, blockConfig, workbench, config.branchLengthRange, origins)
}
