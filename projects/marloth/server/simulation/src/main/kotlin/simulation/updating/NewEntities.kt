package simulation.updating

import mythic.ent.IdSource
import simulation.entities.applyBuffsFromEvents
import simulation.entities.newAccessories
import simulation.happenings.Events
import simulation.main.Deck
import simulation.main.mergeDecks

fun newAccessoriesDeck(events: Events, deck: Deck): Deck =
    Deck(
        accessories = newAccessories(events, deck)
    )

fun newEntities(events: Events, nextId: IdSource): (Deck) -> Deck = { deck ->
  val additions = listOf(
      newAccessoriesDeck(events, deck)
  )
      .plus(applyBuffsFromEvents(deck, nextId, events))

  mergeDecks(listOf(deck).plus(additions))
}
