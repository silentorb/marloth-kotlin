package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.Text
import silentorb.mythic.accessorize.AccessoryDefinition
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.accessorize.RelativeModifier

fun modifiers(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryId.resistanceCold to AccessoryDefinition(
        name = Text.id_resistanceCold,
        modifiers = listOf(
            RelativeModifier(
                type = AccessoryId.resistanceCold,
                strength = 10
            )
        )
    ),
    AccessoryId.resistanceFire to AccessoryDefinition(
        name = Text.id_resistanceFire,
        modifiers = listOf(
            RelativeModifier(
                type = AccessoryId.resistanceFire,
                strength = 10
            )
        )
    ),
    AccessoryId.resistancePoison to AccessoryDefinition(
        name = Text.id_resistancePoison,
        modifiers = listOf(
            RelativeModifier(
                type = AccessoryId.resistancePoison,
                strength = 50
            )
        )
    ),
    AccessoryId.entangled to AccessoryDefinition(
        name = Text.id_entangled
    ),
    AccessoryId.mobile to AccessoryDefinition(
        name = Text.id_mobile
    )
)
