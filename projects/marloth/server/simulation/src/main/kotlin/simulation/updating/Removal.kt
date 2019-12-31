package simulation.updating

import silentorb.mythic.aura.SoundDurations
import silentorb.mythic.aura.finishedSounds
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.mapEntryValue
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.Events
import simulation.entities.cleanupAttachmentSource
import silentorb.mythic.timing.expiredTimers
import simulation.happenings.PurchaseEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.main.removeEntities

val cleanupOutdatedReferences: (Deck) -> Deck = { deck ->
  deck.copy(
      attachments = deck.attachments
          .mapValues(mapEntryValue(cleanupAttachmentSource(deck)))
  )
}

fun getFinished(soundDurations: SoundDurations, events: Events, deck: Deck): Set<Id> {
  return expiredTimers(deck.timersFloat, deck.timersInt)
      .plus(events.filterIsInstance<TakeItemEvent>().map { it.item })
      .plus(finishedSounds(soundDurations)(deck.sounds))
}

// Gathers all entities that need to be deleted
// and for each entity, all records of all types are deleted that have the same key as that entity
fun removeWhole(soundDurations: SoundDurations, events: Events, deck: Deck): (Deck) -> Deck = { aggregator ->
  val finished = getFinished(soundDurations, events, deck)
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
