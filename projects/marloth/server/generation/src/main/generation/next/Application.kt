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

fun buildArchitecture(generationConfig: GenerationConfig, dice: Dice, grid: MapGrid,
                      appliedPolyominoes: List<AppliedPolyomino>,
                      polyominoMap: PolyominoMap,
                      cellBiomes: CellBiomeMap,
                      builders: Map<Polyomino, Builder>): List<Hand> {

  return appliedPolyominoes.flatMap { applied ->
    val polyomino = applied.polyomino
    val polyominoInfo = polyominoMap[polyomino]!!
    val biomeName = cellBiomes[applied.position]!!
    val position = applied.position + polyominoInfo.offset
    val builder = builders[polyominoInfo.original]
    if (builder == null)
      throw Error("Could not find builder for polyomino")

    val input = BuilderInput(
        config = generationConfig,
        cellBiomes = cellBiomes,
        dice = dice,
        position = applyCellPosition(position),
        turns = polyominoInfo.turns,
        grid = grid,
        cell = position,
        biome = generationConfig.biomes[biomeName]!!
    )
    builder(input)
  }
}
