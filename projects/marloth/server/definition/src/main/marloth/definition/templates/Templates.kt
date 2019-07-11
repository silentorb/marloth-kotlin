package marloth.definition.templates

import scenery.enums.BuffId
import simulation.combat.DamageType
import simulation.main.HandTemplates

val templates: HandTemplates = mapOf(
    BuffId.burning.name to damageBuff(DamageType.fire),
    BuffId.chilled.name to damageBuff(DamageType.cold),
    BuffId.poisoned.name to damageBuff(DamageType.poison)
)
