package simulation.misc

import silentorb.mythic.characters.Freedom
import silentorb.mythic.characters.FreedomTable
import silentorb.mythic.characters.Freedoms
import silentorb.mythic.ent.Id
import simulation.main.Deck

fun getFreedoms(deck: Deck): (Id) -> Freedoms = { id ->
  val character = deck.characters[id]!!
  if (!character.isAlive)
    Freedom.none
  else if (deck.performances.any { it.value.target == id })
    Freedom.orbiting
  else
    Freedom.all
}

// TODO: This is called more than once with the same input data per game loop.  The plan is to cache this.
fun getFreedomTable(deck: Deck): FreedomTable =
    deck.characterRigs.keys.associateWith(getFreedoms(deck))
