package generation.next

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
