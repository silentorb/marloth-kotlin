package generation.next

import generation.misc.GenerationConfig
import randomly.Dice
import simulation.main.Hand

data class BuilderInput(
    val turns: Int,
    val dice: Dice,
    val config: GenerationConfig
)

typealias Builder = (BuilderInput) -> List<Hand>
