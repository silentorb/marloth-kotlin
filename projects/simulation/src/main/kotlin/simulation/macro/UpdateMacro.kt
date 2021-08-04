package simulation.macro

import simulation.main.Deck
import simulation.main.World

fun updateBushes(duration: Frames, deck: Deck): Deck {
  return deck
}

fun updateMacro(duration: Frames, world: World): World {
  val deck = world.deck
  val nextDeck = listOf(::updateBushes)
      .fold(deck) { a, b -> b(duration, a) }

  return world.copy(
      deck = nextDeck,
  )
}
