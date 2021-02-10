package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import marloth.scenery.enums.DevText
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName

fun accessories(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryId.entangled to AccessoryDefinition(
        name = DevText("Entangled"),
        many = false,
    ),
    AccessoryId.entangling to AccessoryDefinition(
        name = DevText("Entangling"),
        many = false,
    )
)
