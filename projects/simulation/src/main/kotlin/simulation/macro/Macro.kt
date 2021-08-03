package simulation.macro

import simulation.main.Deck
import simulation.main.World

typealias MacroDeckUpdater = (Int, Deck) -> Deck

fun updateBushes(duration: Int, deck: Deck): Deck {
  return deck
}

fun updateMacro(duration: Int, world: World): World {
  val deck = world.deck
  val nextDeck = listOf(::updateBushes)
      .fold(deck) { a, b -> b(duration, a) }

  return world.copy(
      deck = nextDeck,
  )
}
