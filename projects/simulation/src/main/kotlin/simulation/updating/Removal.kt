package simulation.updating

import silentorb.mythic.aura.SoundDurations
import silentorb.mythic.aura.finishedSounds
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.expiredTimers
import simulation.accessorize.Accessory
import simulation.entities.PruneEntityEvent
import simulation.happenings.TakeItemEvent
import simulation.main.Deck
import simulation.main.removeEntities

val cleanOutdatedReferences: (Deck) -> Deck = { deck ->
  deck.copy(
//      attachments = deck.attachments
//          .mapValues(mapEntryValue(cleanupAttachmentSource(deck)))
  )
}

fun getFinished(soundDurations: SoundDurations, events: Events, deck: Deck): Set<Id> {
  return expiredTimers(deck.timersFloat, deck.timersInt)
      .plus(events.filterIsInstance<TakeItemEvent>().map { it.item })
      .plus(finishedSounds(soundDurations)(deck.sounds))
}

fun exhaustedAccessories(accessories: Table<Accessory>) =
    accessories.filterValues {
      it.removeOnEmpty && it.quantity < 1
    }.keys

// Gathers all entities that need to be deleted
// and for each entity, all records of all types are deleted that have the same key as that entity
fun removeWhole(soundDurations: SoundDurations, events: Events, deck: Deck): (Deck) -> Deck = { aggregator ->
  val finished = getFinished(soundDurations, events, deck)
      .plus(events.filterIsInstance<DeleteEntityEvent>().map { it.id })
      .plus(events.filterIsInstance<PruneEntityEvent>().map { it.id })
      .plus(exhaustedAccessories(aggregator.accessories))

  removeEntities(finished)(aggregator)
}

//fun pruneSoldWares(events: Events, deck: Deck): (Deck) -> Deck = { aggregator ->
//  val wares = deck.vendors
//  val next = aggregator.copy(
//      vendors = wares.filterKeys { id ->
//        events.filterIsInstance<PurchaseEvent>().none { it.ware == id }
//      }
//  )
//  next
//}

// Removes particular components of an entity without removing all components of that entity
//fun removePartial(events: Events, previous: Deck): (Deck) -> Deck =
//    pipe(
//        pruneSoldWares(events, previous)
//    )
