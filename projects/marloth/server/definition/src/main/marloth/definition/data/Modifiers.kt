package marloth.definition.data

import scenery.enums.ModifierId
import scenery.enums.ModifierType
import scenery.enums.Text
import simulation.combat.DamageType
import simulation.entities.ModifierDefinition
import simulation.happenings.DamageAction

val staticModifiers: Map<ModifierId, ModifierDefinition> = mapOf(
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
    )
)
