package marloth.definition.misc

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

fun enemyDistributions(): Map<String, Int> =
    if (getDebugString("MONSTER_TYPE") != null)
      mapOf(
          getDebugString("MONSTER_TYPE")!! to 1
      )
    else
      mapOf(
          CreatureId.hogMan to 4,
          CreatureId.sentinel to 12,
          CreatureId.hound to 8
      )

fun scalingDistributions(): DistributionMap = mapOf(
    DistributionGroup.none to 25,
    DistributionGroup.monster to 2
)

fun monsterLimit() = getDebugInt("MONSTER_LIMIT") ?: 1000
