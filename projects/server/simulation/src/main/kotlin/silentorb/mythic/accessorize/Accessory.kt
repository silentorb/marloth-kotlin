package silentorb.mythic.accessorize

import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.scenery.MeshName
import simulation.entities.DepictionType

data class Accessory(
    val type: AccessoryName,
    val target: Id
)

typealias AccessoryName = String

data class AccessoryDefinition(
    val name: Text,
    val modifiers: List<RelativeModifier> = listOf(),
    //This mesh field is a stopgap until attaching any depiction to an articulation is supported
    val mesh: MeshName? = null
)

fun getAccessories(accessories: Table<Accessory>, entity: Id): Table<Accessory> {
  return accessories.filterValues { it.target == entity }
}
