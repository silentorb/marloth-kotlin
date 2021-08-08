package simulation.macro

import simulation.main.Deck
import simulation.main.Frames
import simulation.main.World
import simulation.misc.Definitions

fun updateBushes(frames: Frames, deck: Deck): Deck {
  return deck
}

fun updateMacro(definitions: Definitions, frames: Frames, world: World): World {
  val deck = world.deck
  val nextDeck = listOf(::updateBushes)
      .fold(deck) { a, b -> b(frames, a) }

  return world.copy(
      deck = nextDeck,
  )
}
