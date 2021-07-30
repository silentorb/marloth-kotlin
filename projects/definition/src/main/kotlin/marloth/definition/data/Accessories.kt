package marloth.definition.data

import marloth.scenery.enums.AccessoryIdOld
import marloth.scenery.enums.DevText
import simulation.accessorize.AccessoryDefinition
import simulation.accessorize.AccessoryName

object Accessories {
    val apple = "apple"
    val apple2 = "apple2"
    val apple3 = "apple3"
    val berries = "berries"
    val cancelShadowSpirit = "cancelShadowSpirit"
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
