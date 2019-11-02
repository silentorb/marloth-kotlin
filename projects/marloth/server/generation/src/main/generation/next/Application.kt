package generation.next

import generation.elements.Polyomino
import generation.elements.convertGridToElements
import generation.misc.GenerationConfig
import mythic.spatial.Vector3
import mythic.spatial.toVector3
import randomly.Dice
import simulation.main.Hand
import simulation.misc.MapGrid

data class BuilderInput(
    val config: GenerationConfig,
    val dice: Dice,
    val turns: Int,
    val position: Vector3
)

typealias Builder = (BuilderInput) -> List<Hand>

fun buildArchitecture(generationConfig: GenerationConfig, dice: Dice, grid: MapGrid,
                      polyominoes: Set<Polyomino>, builders: Map<Polyomino, Builder>): List<Hand> {

  val appliedPolyominoes = convertGridToElements(grid, polyominoes)
  return appliedPolyominoes.flatMap { applied ->
    val polyomino = applied.polyomino
    val position = applied.position
    val builder = builders[polyomino]
    if (builder == null)
      throw Error("Could not find builder for polyomino")

    val input = BuilderInput(
        config = generationConfig,
        dice = dice,
        turns = 0,
        position = position.toVector3() * 10f
    )
    builder(input)
  }
}
