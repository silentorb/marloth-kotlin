package simulation.entities

import silentorb.mythic.ent.Id
import simulation.accessorize.AccessoryName

data class Ware(
    val type: AccessoryName,
    val quantity: Int? = null,
    val price: Int,
)

data class Vendor(
    val wares: Map<Id, Ware>,
)
