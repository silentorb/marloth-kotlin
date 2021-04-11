package marloth.definition.data

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.DevText
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName

object Accessories {
    val shadowSpirit = "shadowSpirit"
}

fun accessories(): Map<AccessoryName, AccessoryDefinition> = mapOf(
    AccessoryIdOld.entangled to AccessoryDefinition(
        name = DevText("Entangled"),
        many = false,
    ),
    AccessoryIdOld.entangling to AccessoryDefinition(
        name = DevText("Entangling"),
        many = false,
    )
)
