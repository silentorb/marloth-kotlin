package marloth.definition.data

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.TextId
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName
import simulation.accessorize.ChildAccessory

fun modifiers(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryIdOld.resistanceCold to AccessoryDefinition(
        name = TextId.id_resistanceCold,
        children = listOf(
            ChildAccessory(
                type = AccessoryIdOld.resistanceCold,
                level = 10
            )
        )
    ),
    AccessoryIdOld.resistanceFire to AccessoryDefinition(
        name = TextId.id_resistanceFire,
        children = listOf(
            ChildAccessory(
                type = AccessoryIdOld.resistanceFire,
                level = 10
            )
        )
    ),
    AccessoryIdOld.resistancePoison to AccessoryDefinition(
        name = TextId.id_resistancePoison,
        children = listOf(
            ChildAccessory(
                type = AccessoryIdOld.resistancePoison,
                level = 50
            )
        )
    ),
    AccessoryIdOld.dashing to AccessoryDefinition(
        name = TextId.unnamed,
        debugName = "dashing"
    ),
    AccessoryIdOld.entangled to AccessoryDefinition(
        name = TextId.id_entangled
    ),
    AccessoryIdOld.mobile to AccessoryDefinition(
        name = TextId.id_mobile
    ),
    AccessoryIdOld.graveDigger to AccessoryDefinition(
        name = TextId.id_graveDigger,
        description = TextId.id_graveDiggerDescription
    )
)
