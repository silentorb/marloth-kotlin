package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import simulation.accessorize.AccessoryName

fun selectableAccessories(): Set<AccessoryName> = setOf(
    AccessoryId.dash,
    AccessoryId.entangle,
    AccessoryId.graveDigger
)
