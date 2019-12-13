package simulation.updating

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.pipe
import simulation.happenings.Events
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.main.removeEntities

fun expiredTimers(deck: Deck): Set<Id> =
    setOf<Id>()
        .plus(
            deck.timers
                .filter { it.value.duration < 1 }
                .keys
        )
        .plus(
            deck.timersFloat
                .filter { it.value.duration <= 0f }
                .keys
        )

fun getFinished(events: Events, deck: Deck): Set<Id> {
  return expiredTimers(deck)
      .plus(events.filterIsInstance<TakeItemEvent>().map { it.item })
}

// Gathers all entities that need to be deleted
// and for each entity, all records of all types are deleted that have the same key as that entity
fun removeWhole(events: Events): (Deck) -> Deck = { deck ->
  val finished = getFinished(events, deck)
  if (finished.size > 0) {
    val k = 0
  }
  removeEntities(deck, finished)
}

fun removeSoldWares(events: Events): (Deck) -> Deck = { deck ->
  val wares = deck.wares
  val next = deck.copy(
      wares = wares.filterKeys { id ->
        events.filterIsInstance<PurchaseEvent>().none { it.ware == id }
      }
  )
  next
}

// Removes select components of an entity without removing all components of an entity
fun removePartial(events: Events): (Deck) -> Deck =
    pipe(
        removeSoldWares(events)
    )
