package silentorb.mythic.accessorize

import marloth.scenery.enums.Text
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.scenery.MeshName

data class Accessory(
    val type: AccessoryName,
    val owner: Id
)

typealias AccessoryName = String

data class AccessoryDefinition(
    val name: Text,
    val modifiers: List<RelativeModifier> = listOf(),
    //This mesh field is a stopgap until attaching any depiction to an articulation is supported
    val equippedMesh: MeshName? = null
)

fun getAccessories(accessories: Table<Accessory>, entity: Id): Table<Accessory> {
  return accessories.filterValues { it.owner == entity }
}

data class ChangeItemOwnerEvent(
    val item: Id,
    val newOwner: Id
) : GameEvent

fun updateAccessory(events: Events): (Id, Accessory) -> Accessory {
  val changeOwnerEvents = events.filterIsInstance<ChangeItemOwnerEvent>()
  return { id, accessory ->
    // Currently if two change owner events are triggered at the same time it is random which one
    // is honored
    val change = changeOwnerEvents.firstOrNull { it.item == id }
    if (change != null) {
      accessory.copy(
          owner = change.newOwner
      )
    } else
      accessory
  }
}
