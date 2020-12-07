package simulation.misc

import simulation.accessorize.Accessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.happenings.PurchaseEvent
import simulation.main.Deck
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
                      charges = definition?.charges,
                  )
              )
          )
        }
