package generation.next

import generation.abstracted.HorrorVacuiConfig
import generation.abstracted.horrorVacui
import generation.abstracted.newWindingWorkbench
import generation.abstracted.windingPath
import generation.architecture.definition.BlockDefinitions
import generation.architecture.definition.independentConnectionTypes
import generation.architecture.definition.openConnectionTypes
import generation.elements.*
import generation.misc.BiomeInfo
import generation.misc.GenerationConfig
import mythic.ent.pipe
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import randomly.Dice
import simulation.main.Hand
import simulation.misc.CellBiomeMap
import simulation.misc.MapGrid

data class BuilderInput(
    val config: GenerationConfig,
    val grid: MapGrid,
    val cellBiomes: CellBiomeMap,
    val biome: BiomeInfo,
    val dice: Dice,
    val turns: Int,
    val cell: Vector3i,
    val position: Vector3,
    val sides: Sides
)

typealias Builder = (BuilderInput) -> List<Hand>

fun buildArchitecture(generationConfig: GenerationConfig, dice: Dice,
                      independentConnections: Set<Any>,
                      workbench: Workbench,
                      blockMap: BlockMap,
                      cellBiomes: CellBiomeMap,
                      builders: Map<Block, Builder>): List<Hand> {
  return workbench.blockGrid.flatMap { (position, block) ->
    val info = blockMap[block]!!
    val biomeName = cellBiomes[position]!!
    val builder = builders[info.original]
    if (builder == null)
      throw Error("Could not find builder for polyomino")

    val input = BuilderInput(
        config = generationConfig,
        cellBiomes = cellBiomes,
        dice = dice,
        position = applyCellPosition(position),
        turns = info.turns,
        grid = workbench.mapGrid,
        cell = position,
        biome = generationConfig.biomes[biomeName]!!,
        sides = getUsableCellSides(independentConnections, workbench.blockGrid, position)
    )
    builder(input)
  }
}

fun newWorkbench(dice: Dice, blocks: Set<Block>, independentConnections: Set<Any>, roomCount: Int): Workbench {
  val blockConfig = BlockConfig(
      blocks = blocks,
      independentConnections = independentConnections,
      openConnections = openConnectionTypes()
  )
  val firstBlockVariable = System.getenv("FIRST_BLOCK")
  val firstBlock = if (firstBlockVariable != null)
    getMember(BlockDefinitions, firstBlockVariable as String)
  else
    BlockDefinitions.singleCellRoom

  return pipe(
      windingPath(dice, blockConfig, roomCount),
      horrorVacui(dice, blockConfig, HorrorVacuiConfig(branchRate = 0.7f, branchLengthRange = 1..2))
  )(newWindingWorkbench(firstBlock.block))
}
