package marloth.definition.data

import marloth.definition.newResistanceModifier
import scenery.enums.ModifierDirection
import scenery.enums.ModifierId
import scenery.enums.ModifierType
import scenery.enums.Text
import simulation.combat.DamageType
import simulation.combat.ModifierOperation
import simulation.entities.ModifierDefinition
import simulation.happenings.DamageAction
import simulation.misc.ValueModifier
import simulation.misc.ValueModifierDirection

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
    ModifierId.resistanceCold to newResistanceModifier(Text.id_coldResistance, DamageType.cold),
    ModifierId.resistanceFire to newResistanceModifier(Text.id_fireResistance, DamageType.fire),
    ModifierId.resistancePoison to newResistanceModifier(Text.id_poisonResistance, DamageType.poison)
)
