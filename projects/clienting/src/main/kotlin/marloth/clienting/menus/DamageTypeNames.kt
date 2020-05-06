package marloth.clienting.menus

import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.Text
import simulation.combat.general.DamageType

val damageTypeNames: Map<DamageType, Text> = mapOf(
    DamageTypes.cold to Text.damageType_cold,
    DamageTypes.fire to Text.damageType_fire,
    DamageTypes.poison to Text.damageType_poison
)
