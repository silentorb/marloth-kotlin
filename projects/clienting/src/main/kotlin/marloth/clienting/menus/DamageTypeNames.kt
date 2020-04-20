package marloth.clienting.menus

import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.Text
import simulation.combat.general.DamageType

val damageTypeNames: Map<DamageType, Text> = mapOf(
    DamageTypes.cold.name to Text.damageType_cold,
    DamageTypes.fire.name to Text.damageType_fire,
    DamageTypes.poison.name to Text.damageType_poison
)
