package marloth.definition.templates

import marloth.definition.BuffId
import simulation.combat.DamageType
import simulation.evention.ApplyBuff
import simulation.evention.DamageAction
import simulation.evention.Trigger
import simulation.main.Hand
import simulation.main.HandTemplates

val templates: HandTemplates = mapOf(
    BuffId.burning.name to damageBuff(DamageType.fire),
    BuffId.chilled.name to damageBuff(DamageType.cold),
    BuffId.poisoned.name to damageBuff(DamageType.poison)
)
