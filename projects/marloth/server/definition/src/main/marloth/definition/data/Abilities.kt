package marloth.definition.data

import scenery.enums.AccessoryId
import scenery.enums.ModifierId
import scenery.enums.Text
import simulation.entities.AccessoryDefinition
import simulation.entities.Modifier

val accessories: Map<AccessoryId, AccessoryDefinition> = mapOf(
    AccessoryId.coldResistance to AccessoryDefinition(
        name = Text.id_coldResistance,
        modifiers = listOf(
            Modifier(
                type = ModifierId.resistanceCold,
                strength = 10
            )
        )
    ),
    AccessoryId.fireResistance to AccessoryDefinition(
        name = Text.id_fireResistance,
        modifiers = listOf(
            Modifier(
                type = ModifierId.resistanceFire,
                strength = 10
            )
        )
    ),
    AccessoryId.poisonResistance to AccessoryDefinition(
        name = Text.id_poisonResistance,
        modifiers = listOf(
            Modifier(
                type = ModifierId.resistancePoison,
                strength = 10
            )
        )
    )
)
