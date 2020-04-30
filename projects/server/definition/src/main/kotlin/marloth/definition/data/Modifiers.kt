package marloth.definition.data

import marloth.definition.newResistanceModifier
import marloth.scenery.enums.ModifierId
import marloth.scenery.enums.Text
import marloth.scenery.enums.DamageTypes
import silentorb.mythic.accessorize.ModifierDefinition
import simulation.happenings.DamageAction

fun staticModifiers(): Map<ModifierId, ModifierDefinition> = mapOf(
    ModifierId.damageBurning to ModifierDefinition(
        name = Text.id_damageBurning,
        overTime = DamageAction(
            damageType = DamageTypes.fire.name,
            amount = 0
        )
    ),
    ModifierId.damageChilled to ModifierDefinition(
        name = Text.id_damageChilled,
        overTime = DamageAction(
            damageType = DamageTypes.cold.name,
            amount = 0
        )
    ),
    ModifierId.damagePoisoned to ModifierDefinition(
        name = Text.id_damagePoisoned,
        overTime = DamageAction(
            damageType = DamageTypes.poison.name,
            amount = 0
        )
    ),
    ModifierId.mobile to ModifierDefinition(
        name = Text.id_mobile
    ),

    ModifierId.resistanceCold to newResistanceModifier(Text.id_resistanceCold, DamageTypes.cold.name),
    ModifierId.resistanceFire to newResistanceModifier(Text.id_resistanceFire, DamageTypes.fire.name),
    ModifierId.resistancePoison to newResistanceModifier(Text.id_resistancePoison, DamageTypes.poison.name)
)
