package marloth.definition.data

import marloth.definition.misc.newResistanceModifier
import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Text
import marloth.scenery.enums.DamageTypes
import marloth.scenery.enums.TextId
import simulation.accessorize.AccessoryName
import simulation.accessorize.ModifierDefinition
import simulation.happenings.DamageAction

fun staticModifiers(): Map<AccessoryName, ModifierDefinition> = mapOf(
    AccessoryId.damageBurning to ModifierDefinition(
        name = TextId.id_damageBurning,
        overTime = DamageAction(
            damageType = DamageTypes.fire,
            amount = 0
        )
    ),
    AccessoryId.damageChilled to ModifierDefinition(
        name = TextId.id_damageChilled,
        overTime = DamageAction(
            damageType = DamageTypes.cold,
            amount = 0
        )
    ),
    AccessoryId.damagePoisoned to ModifierDefinition(
        name = TextId.id_damagePoisoned,
        overTime = DamageAction(
            damageType = DamageTypes.poison,
            amount = 0
        )
    ),

    AccessoryId.entangled to ModifierDefinition(
        name = TextId.id_entangled
    ),

    AccessoryId.entangleImmune to ModifierDefinition(
        name = TextId.unnamed
    ),
    
    AccessoryId.mobile to ModifierDefinition(
        name = TextId.id_mobile
    ),

    AccessoryId.resistanceCold to newResistanceModifier(TextId.id_resistanceCold, DamageTypes.cold),
    AccessoryId.resistanceFire to newResistanceModifier(TextId.id_resistanceFire, DamageTypes.fire),
    AccessoryId.resistancePoison to newResistanceModifier(TextId.id_resistancePoison, DamageTypes.poison)
)
