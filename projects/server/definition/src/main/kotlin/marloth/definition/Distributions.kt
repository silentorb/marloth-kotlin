package marloth.definition

import marloth.definition.data.creatures

enum class DistributionGroup {
  cloud,
  key,
  merchant,
  monster,
  none,
  treasureChest
}

typealias DistributionMap = Map<DistributionGroup, Int>

fun enemyDistributions() = mapOf(
    creatures.hogMan to 5,
    creatures.sentinel to 12,
    creatures.hound to 10
)

fun scalingDistributions(): DistributionMap = mapOf(
    DistributionGroup.none to 10,
    DistributionGroup.monster to 2
)

fun fixedDistributions(): DistributionMap =
    mapOf(
        DistributionGroup.monster to 3,
        DistributionGroup.key to 3
    )
