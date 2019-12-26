package marloth.clienting.gui

import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.Text
import silentorb.mythic.combat.DamageType

val damageTypeNames: Map<DamageType, Text> = mapOf(
    DamageTypes.cold.name to Text.damageType_cold,
    DamageTypes.fire.name to Text.damageType_fire,
    DamageTypes.poison.name to Text.damageType_poison
)
