package marloth.definition.data

import marloth.definition.misc.newResistanceModifier
import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.TextId
import simulation.accessorize.AccessoryName
import simulation.accessorize.ModifierDefinition
import simulation.happenings.DamageAction

fun staticModifiers(): Map<AccessoryName, ModifierDefinition> = mapOf(
    AccessoryIdOld.damageBurning to ModifierDefinition(
        name = TextId.id_damageBurning,
        overTime = DamageAction(
            damageType = DamageTypes.fire,
            amount = 0
        )
    ),
    AccessoryIdOld.damageChilled to ModifierDefinition(
        name = TextId.id_damageChilled,
        overTime = DamageAction(
            damageType = DamageTypes.cold,
            amount = 0
        )
    ),
    AccessoryIdOld.damagePoisoned to ModifierDefinition(
        name = TextId.id_damagePoisoned,
        overTime = DamageAction(
            damageType = DamageTypes.poison,
            amount = 0
        )
    ),

    AccessoryIdOld.entangled to ModifierDefinition(
        name = TextId.id_entangled
    ),

    AccessoryIdOld.entangleImmune to ModifierDefinition(
        name = TextId.unnamed
    ),
    
    AccessoryIdOld.mobile to ModifierDefinition(
        name = TextId.id_mobile
    ),

    AccessoryIdOld.resistanceCold to newResistanceModifier(TextId.id_resistanceCold, DamageTypes.cold),
    AccessoryIdOld.resistanceFire to newResistanceModifier(TextId.id_resistanceFire, DamageTypes.fire),
    AccessoryIdOld.resistancePoison to newResistanceModifier(TextId.id_resistancePoison, DamageTypes.poison)
)
