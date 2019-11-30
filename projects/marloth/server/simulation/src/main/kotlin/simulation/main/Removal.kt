package simulation.main

import mythic.ent.Id
import mythic.ent.pipe
import simulation.happenings.OrganizedEvents

fun expiredTimers(deck: Deck): Set<Id> =
    deck.timers
        .filter { it.value.duration < 1 }
        .keys

fun getFinished(events: OrganizedEvents, deck: Deck): Set<Id> {
  return expiredTimers(deck)
      .plus(events.takeItems.map { it.item })
}

// Gathers all entities that need to be deleted
// and for each entity, all records of all types are deleted that have the same key as that entity
fun removeWhole(events: OrganizedEvents): (Deck) -> Deck = { deck ->
  val finished = getFinished(events, deck)
  if (finished.size > 0) {
    val k = 0
  }
  removeEntities(deck, finished)
}

fun removeSoldWares(events: OrganizedEvents): (Deck) -> Deck = { deck ->
  val wares = deck.wares
  val next = deck.copy(
      wares = wares.filterKeys { id ->
        events.purchases.none { it.ware == id }
      }
  )
  next
}

// Removes select components of an entity without removing all components of an entity
fun removePartial(events: OrganizedEvents): (Deck) -> Deck =
    pipe(
        removeSoldWares(events)
    )
