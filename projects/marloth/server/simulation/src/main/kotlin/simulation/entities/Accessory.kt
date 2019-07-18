package simulation.entities

import mythic.ent.Id
import scenery.enums.AccessoryId
import simulation.happenings.OrganizedEvents
import simulation.main.Deck

data class Accessory(
    val type: AccessoryId
)

fun newAccessories(events: OrganizedEvents, deck: Deck): Map<Id, Accessory> = events.purchases.map { purchase ->
  Pair(purchase.ware, Accessory(type = purchase.wareType))
}.associate { it }
