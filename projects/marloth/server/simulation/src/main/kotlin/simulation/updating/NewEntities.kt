package simulation.updating

import silentorb.mythic.ent.IdSource
import simulation.entities.AnimationDurationMap
import simulation.entities.applyBuffsFromEvents
import simulation.entities.newAccessories
import simulation.entities.performancesFromEvents
import simulation.happenings.Events
import simulation.main.Deck
import simulation.main.allHandsOnDeck
import simulation.main.mergeDecks
import simulation.misc.Definitions

fun newAccessoriesDeck(events: Events, deck: Deck): Deck =
    Deck(
        accessories = newAccessories(events, deck)
    )

fun newEntities(definitions: Definitions, animationDurations: AnimationDurationMap, events: Events, nextId: IdSource): (Deck) -> Deck = { deck ->
  val hands = listOf(
      performancesFromEvents(definitions, animationDurations, deck, events)
  )
      .flatten()

  val additions = listOf(
      newAccessoriesDeck(events, deck)
  )
      .plus(allHandsOnDeck(hands, nextId, Deck()))
      .plus(applyBuffsFromEvents(deck, nextId, events))

  listOf(deck).plus(additions).reduce(mergeDecks)
}
