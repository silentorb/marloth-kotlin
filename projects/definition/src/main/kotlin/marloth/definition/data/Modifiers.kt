package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.TextId
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName
import simulation.accessorize.ChildAccessory

fun modifiers(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryId.resistanceCold to AccessoryDefinition(
        name = TextId.id_resistanceCold,
        children = listOf(
            ChildAccessory(
                type = AccessoryId.resistanceCold,
                level = 10
            )
        )
    ),
    AccessoryId.resistanceFire to AccessoryDefinition(
        name = TextId.id_resistanceFire,
        children = listOf(
            ChildAccessory(
                type = AccessoryId.resistanceFire,
                level = 10
            )
        )
    ),
    AccessoryId.resistancePoison to AccessoryDefinition(
        name = TextId.id_resistancePoison,
        children = listOf(
            ChildAccessory(
                type = AccessoryId.resistancePoison,
                level = 50
            )
        )
    ),
    AccessoryId.dashing to AccessoryDefinition(
        name = TextId.unnamed,
        debugName = "dashing"
    ),
    AccessoryId.entangled to AccessoryDefinition(
        name = TextId.id_entangled
    ),
    AccessoryId.mobile to AccessoryDefinition(
        name = TextId.id_mobile
    ),
    AccessoryId.graveDigger to AccessoryDefinition(
        name = TextId.id_graveDigger,
        description = TextId.id_graveDiggerDescription
    )
)
