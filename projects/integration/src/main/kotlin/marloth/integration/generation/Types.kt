package marloth.integration.generation

import generation.architecture.engine.GenerationConfig
import silentorb.mythic.ent.IdSource
import silentorb.mythic.randomly.Dice
import simulation.main.NewHand

data class DistributionConfig(
    val cellCount: Int,
    val level: Int,
    val dice: Dice,
)

typealias SlotSelector = (DistributionConfig, SlotMap) -> SlotMap
typealias HandGenerator = (GenerationConfig, IdSource, Dice, SlotMap) -> List<NewHand>
typealias PropFilter = (Collection<String>) -> Boolean

data class Distributor(
    val select: SlotSelector,
    val generate: HandGenerator
)
