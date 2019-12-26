package silentorb.mythic.accessorize

import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id

data class Accessory(
    val type: AccessoryName,
    val target: Id
)

typealias AccessoryName = String

data class AccessoryDefinition(
    val name: Text,
    val modifiers: List<RelativeModifier> = listOf()
)
