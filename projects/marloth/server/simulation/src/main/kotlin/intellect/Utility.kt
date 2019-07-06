package intellect

import mythic.ent.Table
import simulation.Deck

fun aliveSpirits(deck: Deck): Table<Spirit> =
    deck.spirits.filterKeys {
      val character = deck.characters[it]!!
      character.isAlive
    }
