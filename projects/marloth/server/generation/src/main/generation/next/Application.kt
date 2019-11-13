package generation.next

import generation.abstracted.newWindingPath
import generation.architecture.definition.BlockDefinitions
import generation.architecture.definition.independentConnectionTypes
import generation.architecture.definition.openConnectionTypes
import generation.elements.*
import generation.misc.BiomeInfo
import generation.misc.GenerationConfig
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
    val position: Vector3
)

typealias Builder = (BuilderInput) -> List<Hand>

fun buildArchitecture(generationConfig: GenerationConfig, dice: Dice, workbench: Workbench,
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
        biome = generationConfig.biomes[biomeName]!!
    )
    builder(input)
  }
}

//fun mapBlocksOpenConnections(openConnections: Set<Any>, blocks: Set<Block>): Map<Block, Block> =
//    blocks.associateWith { block ->
//      block.copy(
//          sides = block.sides.mapValues { side -> side.value.intersect(openConnections) }
//      )
//    }

fun newWorkbench(dice: Dice, blocks: Set<Block>, roomCount: Int): Workbench {
//  val openConnections = openConnectionTypes()
  val blockConfig = BlockConfig(
      blocks = blocks,
//      openConnectionBlocks = mapBlocksOpenConnections(openConnections, blocks),
      independentConnections = independentConnectionTypes(),
      openConnections = openConnectionTypes()
  )
  val firstBlockVariable = System.getenv("FIRST_BLOCK")
  val firstBlock = if (firstBlockVariable != null)
    getMember(BlockDefinitions, firstBlockVariable as String)
  else
    BlockDefinitions.singleCellRoom

  return newWindingPath(dice, blockConfig, roomCount, firstBlock)
}
