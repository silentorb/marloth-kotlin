package marloth.definition

import marloth.definition.data.Creatures
import silentorb.mythic.debugging.getDebugInt

enum class DistributionGroup {
  cloud,
  victoryKey,
  merchant,
  monster,
  none,
  treasureChest
}

typealias DistributionMap = Map<DistributionGroup, Int>

fun enemyDistributions() = mapOf(
    Creatures.hogMan to 4,
    Creatures.sentinel to 12,
    Creatures.hound to 8
)

fun scalingDistributions(): DistributionMap = mapOf(
    DistributionGroup.none to 10,
    DistributionGroup.monster to 2
)

fun fixedDistributions(): DistributionMap =
    mapOf(
        DistributionGroup.monster to 3,
        DistributionGroup.victoryKey to (getDebugInt("VICTORY_KEY_COUNT") ?: 3)
    )
