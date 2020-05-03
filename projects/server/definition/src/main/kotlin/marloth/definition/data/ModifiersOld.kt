package marloth.definition.data

import marloth.definition.misc.newResistanceModifier
import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Text
import marloth.scenery.enums.DamageTypes
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.accessorize.ModifierDefinition
import simulation.happenings.DamageAction

fun staticModifiers(): Map<AccessoryName, ModifierDefinition> = mapOf(
    AccessoryId.damageBurning to ModifierDefinition(
        name = Text.id_damageBurning,
        overTime = DamageAction(
            damageType = DamageTypes.fire.name,
            amount = 0
        )
    ),
    AccessoryId.damageChilled to ModifierDefinition(
        name = Text.id_damageChilled,
        overTime = DamageAction(
            damageType = DamageTypes.cold.name,
            amount = 0
        )
    ),
    AccessoryId.damagePoisoned to ModifierDefinition(
        name = Text.id_damagePoisoned,
        overTime = DamageAction(
            damageType = DamageTypes.poison.name,
            amount = 0
        )
    ),

    AccessoryId.entangled to ModifierDefinition(
        name = Text.id_entangled
    ),

    AccessoryId.entangleImmune to ModifierDefinition(
        name = Text.unnamed
    ),
    
    AccessoryId.mobile to ModifierDefinition(
        name = Text.id_mobile
    ),

    AccessoryId.resistanceCold to newResistanceModifier(Text.id_resistanceCold, DamageTypes.cold.name),
    AccessoryId.resistanceFire to newResistanceModifier(Text.id_resistanceFire, DamageTypes.fire.name),
    AccessoryId.resistancePoison to newResistanceModifier(Text.id_resistancePoison, DamageTypes.poison.name)
)
