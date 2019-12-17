package simulation.updating

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Events
import simulation.entities.expiredTimers
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.main.removeEntities

fun getFinished(events: Events, deck: Deck): Set<Id> {
  return expiredTimers(deck)
      .plus(events.filterIsInstance<TakeItemEvent>().map { it.item })
}

// Gathers all entities that need to be deleted
// and for each entity, all records of all types are deleted that have the same key as that entity
fun removeWhole(events: Events, deck: Deck): (Deck) -> Deck = { aggregator ->
  val finished = getFinished(events, deck)
  if (finished.size > 0) {
    val k = 0
  }
  removeEntities(finished)(aggregator)
}

fun removeSoldWares(events: Events, deck: Deck): (Deck) -> Deck = { aggregator ->
  val wares = deck.wares
  val next = aggregator.copy(
      wares = wares.filterKeys { id ->
        events.filterIsInstance<PurchaseEvent>().none { it.ware == id }
      }
  )
  next
}

// Removes select components of an entity without removing all components of an entity
fun removePartial(events: Events, deck: Deck): (Deck) -> Deck =
    pipe(
        removeSoldWares(events, deck)
    )
