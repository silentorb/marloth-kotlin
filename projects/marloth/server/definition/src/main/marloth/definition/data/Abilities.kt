package marloth.definition.data

import scenery.enums.AccessoryId
import scenery.enums.ModifierId
import scenery.enums.Text
import simulation.entities.AccessoryDefinition
import simulation.entities.AccessoryName
import simulation.entities.Modifier

fun staticAccessories(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryId.resistanceCold.name to AccessoryDefinition(
        name = Text.id_coldResistance,
        modifiers = listOf(
            Modifier(
                type = ModifierId.resistanceCold,
                strength = 10
            )
        )
    ),
    AccessoryId.resistanceFire.name to AccessoryDefinition(
        name = Text.id_fireResistance,
        modifiers = listOf(
            Modifier(
                type = ModifierId.resistanceFire,
                strength = 10
            )
        )
    ),
    AccessoryId.resistancePoison.name to AccessoryDefinition(
        name = Text.id_poisonResistance,
        modifiers = listOf(
            Modifier(
                type = ModifierId.resistancePoison,
                strength = 50
            )
        )
    )
)
