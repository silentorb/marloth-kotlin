package simulation.entities

import mythic.ent.Id
import scenery.enums.AccessoryId
import simulation.happenings.Events
import simulation.happenings.PurchaseEvent
import simulation.main.Deck

data class Accessory(
    val type: AccessoryId
)

fun newAccessories(events: Events, deck: Deck): Map<Id, Accessory> =
    events.filterIsInstance<PurchaseEvent>()
        .map { purchase ->
          Pair(purchase.ware, Accessory(type = purchase.wareType))
        }
        .associate { it }
