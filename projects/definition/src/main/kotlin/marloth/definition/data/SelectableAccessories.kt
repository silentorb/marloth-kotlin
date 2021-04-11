package marloth.definition.data

import marloth.scenery.enums.AccessoryIdOld
import simulation.accessorize.AccessoryName

fun selectableAccessories(): Set<AccessoryName> = setOf(
    AccessoryIdOld.dash,
    AccessoryIdOld.entangle,
    AccessoryIdOld.graveDigger
)
