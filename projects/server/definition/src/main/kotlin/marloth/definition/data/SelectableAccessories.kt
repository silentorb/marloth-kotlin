package marloth.definition.data

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.AccessoryName

fun selectableAccessories(): Set<AccessoryName> = setOf(
    AccessoryId.dash,
    AccessoryId.entangle
)
