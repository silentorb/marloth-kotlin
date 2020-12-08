package marloth.clienting.gui.menus

import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.Text
import marloth.scenery.enums.TextId
import simulation.combat.general.DamageType

val damageTypeNames: Map<DamageType, Text> = mapOf(
    DamageTypes.cold to TextId.damageType_cold,
    DamageTypes.fire to TextId.damageType_fire,
    DamageTypes.poison to TextId.damageType_poison
)
