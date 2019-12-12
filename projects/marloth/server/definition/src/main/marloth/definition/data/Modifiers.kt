package marloth.definition.data

import marloth.definition.newResistanceModifier
import marloth.scenery.enums.ModifierId
import marloth.scenery.enums.Text
import simulation.combat.DamageType
import simulation.entities.ModifierDefinition
import simulation.happenings.DamageAction

fun staticModifiers(): Map<ModifierId, ModifierDefinition> = mapOf(
    ModifierId.damageBurning to ModifierDefinition(
        name = Text.id_damageBurning,
        overTime = DamageAction(
            damageType = DamageType.fire,
            amount = 0
        )
    ),
    ModifierId.damageChilled to ModifierDefinition(
        name = Text.id_damageChilled,
        overTime = DamageAction(
            damageType = DamageType.cold,
            amount = 0
        )
    ),
    ModifierId.damagePoisoned to ModifierDefinition(
        name = Text.id_damagePoisoned,
        overTime = DamageAction(
            damageType = DamageType.poison,
            amount = 0
        )
    ),
    ModifierId.resistanceCold to newResistanceModifier(Text.id_resistanceCold, DamageType.cold),
    ModifierId.resistanceFire to newResistanceModifier(Text.id_resistanceFire, DamageType.fire),
    ModifierId.resistancePoison to newResistanceModifier(Text.id_resistancePoison, DamageType.poison)
)
