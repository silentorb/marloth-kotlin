package marloth.definition.misc

import generation.general.Rarity
import marloth.scenery.enums.CreatureId
import silentorb.mythic.debugging.getDebugInt
import silentorb.mythic.debugging.getDebugString
import kotlin.math.min

enum class DistributionGroup {
  cloud,
  victoryKey,
  merchant,
  monster,
  none,
  treasureChest
}

typealias DistributionMap = Map<DistributionGroup, Int>

fun monsterDistributions(): Map<String, Rarity> =
    if (getDebugString("MONSTER_TYPE") != null)
      mapOf(
          getDebugString("MONSTER_TYPE")!! to Rarity.common
      )
    else
      mapOf(
          CreatureId.hogMan to Rarity.rare,
          CreatureId.sentinel to Rarity.uncommon,
          CreatureId.hound to Rarity.common
      )

fun scalingDistributions(): DistributionMap = mapOf(
    DistributionGroup.none to 25,
    DistributionGroup.monster to 2
)

fun monsterLimit() = getDebugInt("MONSTER_LIMIT") ?: 1000
