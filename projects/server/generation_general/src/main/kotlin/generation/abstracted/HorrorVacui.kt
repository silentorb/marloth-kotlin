package generation.abstracted

import generation.general.BlockConfig
import generation.general.BlockGrid
import generation.general.Workbench
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.CellAttribute
import simulation.misc.CellMap

data class HorrorVacuiConfig(
    val branchRate: Float,
    val branchLengthRange: IntRange
)

//private fun horrorVacuiBranch(dice: Dice, blockConfig: BlockConfig, blockGrid: BlockGrid,
//                              branchLengthRange: IntRange, origins: List<Vector3i>): BlockGrid {
//  return if (origins.any()) {
//    val branchLength = dice.getInt(branchLengthRange)
//    val nextBlockGrid = addPathStep(branchLength, dice, blockConfig, blockGrid, origins.first())
//    horrorVacuiBranch(dice, blockConfig, nextBlockGrid, branchLengthRange, origins.drop(1))
//  } else
//    blockGrid
//}

fun cellIsNotEndpoint(cells: CellMap) = { position: Vector3i ->
  !cells[position]!!.attributes.contains(CellAttribute.home)
}

//fun horrorVacui(dice: Dice, blockConfig: BlockConfig,
//                config: HorrorVacuiConfig): (Workbench) -> Workbench = { workbench ->
//  val blockGrid = workbench.blockGrid
//  val branchCount = (blockGrid.size.toFloat() * config.branchRate).toInt()
//  val possibleCells = workbench.blockGrid.keys
////      .filter(blockCanHaveMoreConnections(blockConfig, blockGrid))
//      .filter(cellIsNotEndpoint(workbench.mapGrid.cells))
//  val origins = dice.shuffle(possibleCells).take(branchCount)
//  horrorVacuiBranch(dice, blockConfig, workbench, config.branchLengthRange, origins)
//}
