package simulation.misc

import simulation.accessorize.Accessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.happenings.PurchaseEvent
import simulation.main.Deck

fun newAccessories(events: Events, deck: Deck): Map<Id, Accessory> =
    events.filterIsInstance<PurchaseEvent>()
        .map { purchase ->
          Pair(purchase.ware, Accessory(type = purchase.wareType, owner = purchase.customer))
        }
        .associate { it }
