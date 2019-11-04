package generation.next

import generation.architecture.cellLength
import generation.elements.Polyomino
import generation.elements.applyCellPosition
import generation.elements.convertGridToElements
import generation.misc.BiomeInfo
import generation.misc.GenerationConfig
import mythic.spatial.Vector3
import mythic.spatial.Vector3i
import mythic.spatial.toVector3
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
                      cellBiomes: CellBiomeMap,
                      polyominoes: Set<Polyomino>, builders: Map<Polyomino, Builder>): List<Hand> {

  val appliedPolyominoes = convertGridToElements(grid, polyominoes)
  return appliedPolyominoes.flatMap { applied ->
    val polyomino = applied.polyomino
    val position = applied.position
    val biomeName = cellBiomes[position]!!
    val builder = builders[polyomino]
    if (builder == null)
      throw Error("Could not find builder for polyomino")

    val input = BuilderInput(
        config = generationConfig,
        cellBiomes = cellBiomes,
        dice = dice,
        turns = 0,
        position = applyCellPosition(position),
        grid = grid,
        cell = position,
        biome = generationConfig.biomes[biomeName]!!
    )
    builder(input)
  }
}
