package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.ModifierId
import marloth.scenery.enums.Text
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.accessorize.RelativeModifier

fun staticAccessories(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryId.resistanceCold.name to AccessoryDefinition(
        name = Text.id_resistanceCold,
        modifiers = listOf(
            RelativeModifier(
                type = ModifierId.resistanceCold,
                strength = 10
            )
        )
    ),
    AccessoryId.resistanceFire.name to AccessoryDefinition(
        name = Text.id_resistanceFire,
        modifiers = listOf(
            RelativeModifier(
                type = ModifierId.resistanceFire,
                strength = 10
            )
        )
    ),
    AccessoryId.resistancePoison.name to AccessoryDefinition(
        name = Text.id_resistancePoison,
        modifiers = listOf(
            RelativeModifier(
                type = ModifierId.resistancePoison,
                strength = 50
            )
        )
    )
)
