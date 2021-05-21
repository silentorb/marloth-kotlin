package simulation.abilities

import marloth.scenery.enums.AccessoryIdOld
import silentorb.mythic.ent.Id
import simulation.characters.getFaction
import simulation.main.Deck
import simulation.misc.Factions

fun graveDiggerDurationModifer(deck: Deck, actor: Id): Float =
    if (getFaction(deck, actor) == Factions.monsters) {
      val graveDigger = deck.accessories.values
          .filter { it.type == AccessoryIdOld.graveDigger }
          .maxBy { it.level }

      val mod = graveDigger?.level?.toFloat() ?: 0f
      mod * 10f
    } else
      0f
