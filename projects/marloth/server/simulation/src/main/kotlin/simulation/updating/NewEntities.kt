package simulation.updating

import simulation.entities.applyBuffsFromEvents
import simulation.misc.newAccessories
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.toIdHands
import silentorb.mythic.happenings.Events
import simulation.entities.*
import simulation.main.Deck
import simulation.main.idHandsToDeck
import simulation.main.mergeDecks
import simulation.misc.Definitions
import simulation.misc.newAmbientSounds

fun newAccessoriesDeck(events: Events, deck: Deck): Deck =
    Deck(
        accessories = newAccessories(events, deck)
    )

fun newEntities(definitions: Definitions, animationDurations: AnimationDurationMap, previous: Deck, events: Events, nextId: IdSource): (Deck) -> Deck = { next ->
  val idHands = listOf(
      newRespawnCountdowns(nextId, previous, next),
      performancesFromEvents(definitions, animationDurations, nextId, previous, events),
      toIdHands(nextId, newAmbientSounds(previous, next))
  )
      .flatten()

  val additions = listOf(
      newAccessoriesDeck(events, previous)
  )
      .plus(idHandsToDeck(idHands))
      .plus(applyBuffsFromEvents(previous, nextId, events))

  listOf(next).plus(additions).reduce(mergeDecks)
}
