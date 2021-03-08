package simulation.entities

import simulation.accessorize.AccessoryName

data class Ware(
    val type: AccessoryName,
    val quantity: Int? = null,
    val price: Int,
)
