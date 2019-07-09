package simulation.main

import mythic.ent.Id

fun getFinished(deck: Deck): Set<Id> {
  return deck.timers.filter { it.value.duration < 1 }
      .keys
}

val removeEntities: (Deck) -> Deck = { deck ->
  val finished = getFinished(deck)
  removeEntities(deck, finished)
}
