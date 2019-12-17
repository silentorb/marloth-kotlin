package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.happenings.PurchaseEvent
import simulation.main.Deck

data class Accessory(
    val type: AccessoryName
)

fun newAccessories(events: Events, deck: Deck): Map<Id, Accessory> =
    events.filterIsInstance<PurchaseEvent>()
        .map { purchase ->
          Pair(purchase.ware, Accessory(type = purchase.wareType))
        }
        .associate { it }
