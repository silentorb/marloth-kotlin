package simulation.misc

import silentorb.mythic.happenings.Events
import simulation.accessorize.Accessory
import simulation.happenings.PurchaseEvent
import simulation.main.NewHand

fun newAccessories(events: Events, definitions: Definitions): List<NewHand> =
    events.filterIsInstance<PurchaseEvent>()
        .map { purchase ->
          val definition = definitions.accessories[purchase.wareType]
          NewHand(
              components = listOf(
                  Accessory(
                      type = purchase.wareType,
                      owner = purchase.customer,
                      quantity = definition?.quantity ?: 1,
                  )
              )
          )
        }
