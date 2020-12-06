package simulation.entities

import silentorb.mythic.ent.Id
import simulation.accessorize.AccessoryName

data class Ware(
    val type: AccessoryName,
    val quantity: Int?,
    val price: Int,
)

data class Merchant(
    val wares: Map<Id, Ware>,
)
