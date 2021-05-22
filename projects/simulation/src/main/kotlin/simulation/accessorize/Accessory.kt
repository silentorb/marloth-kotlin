package simulation.accessorize

import marloth.scenery.enums.Text
import marloth.scenery.enums.TextId
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.scenery.MeshName
import simulation.happenings.UseAction
import simulation.misc.Definitions
import kotlin.math.max
import kotlin.math.min

data class Accessory(
    val type: AccessoryName,
    val level: Int = 1,
    val source: Id = 0,
    val owner: Id? = null,
    val quantity: Int = 1,
)

typealias AccessoryName = String

data class Nutrient(
    val value: Int, // Percentage
)

data class AccessoryDefinition(
    val name: Text,
    val description: Text = TextId.unnamed,
    val children: List<ChildAccessory> = listOf(),
    val equippedMesh: MeshName? = null, // A stopgap until attaching any depiction to an articulation is supported
    val debugName: String? = null,
    val isConsumable: Boolean = false,
    val maxLevel: Int = 1,
    val quantity: Int = 1,
    val components: List<Any> = listOf(),
    val many: Boolean = true, // Whether a character can have multiple instances of this accessory at once
    val upgrades: Set<AccessoryName> = setOf(),
    val level: Int = 1,
)

fun hasAccessory(type: AccessoryName, accessories: Table<Accessory>, actor: Id): Boolean =
    accessories.values.any { it.owner == actor && it.type == type }

fun hasAnyAccessory(types: Collection<AccessoryName>, accessories: Table<Accessory>, actor: Id): Boolean =
    accessories.values.any { it.owner == actor && types.contains(it.type) }

fun getAccessory(type: AccessoryName, accessories: Table<Accessory>, actor: Id): Map.Entry<Id, Accessory>? =
    accessories.entries.firstOrNull { it.value.owner == actor && it.value.type == type }

fun hasAccessory(type: AccessoryName): (Table<Accessory>, Id) -> Boolean = { accessories, actor ->
  hasAccessory(type, accessories, actor)
}

fun getAccessories(accessories: Table<Accessory>, entity: Id): Table<Accessory> {
  return accessories.filterValues { it.owner == entity }
}

data class ChangeItemOwnerEvent(
    val item: Id,
    val newOwner: Id
)

data class ModifyItemQuantityEvent(
    val item: Id,
    val modifier: Int
)

fun updateAccessory(definitions: Definitions, events: Events): (Id, Accessory) -> Accessory {
  val changeOwnerEvents = events.filterIsInstance<ChangeItemOwnerEvent>()
  val choseImprovedAccessoryEvents = events.filterIsInstance<ChooseImprovedAccessory>()
  val allUseEvents = events.filterIsInstance<UseAction>()
  val modifyQuantityCommands = events.filterIsInstance<ModifyItemQuantityEvent>()

  return { id, accessory ->
    val levelIncreases = choseImprovedAccessoryEvents.count {
      it.accessory == accessory.type && it.actor == accessory.owner
    }
    // Currently if two change owner events are triggered at the same time it is random which one
    // is honored
    val ownerChange = changeOwnerEvents.firstOrNull { it.item == id }
    val quantity = accessory.quantity
    val definition = definitions.accessories[accessory.type]
    val quantityMod = modifyQuantityCommands
        .filter { it.item == id }
        .sumBy { it.modifier }

    val consumptionQuantity = if (definition?.isConsumable == true)
      quantity - allUseEvents.count { it.action == id }
    else
      quantity

    accessory.copy(
        owner = ownerChange?.newOwner ?: accessory.owner,
        level = if (levelIncreases > 0)
          min(accessory.level + levelIncreases, definition!!.maxLevel)
        else
          accessory.level,
        quantity = max(0, consumptionQuantity + quantityMod),
    )
  }
}
