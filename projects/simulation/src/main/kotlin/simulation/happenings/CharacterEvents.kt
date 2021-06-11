package simulation.happenings

import marloth.scenery.enums.Sounds
import silentorb.mythic.audio.NewSound
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.randomly.Dice
import simulation.characters.Character
import simulation.main.Deck

fun eventsFromCharacters(deck: Deck, previous: Table<Character>, dice: Dice): Events =
    deck.characters.mapNotNull {
      val previousCounter = previous[it.key]?.stepCounter ?: 0f
      if (it.value.stepCounter < previousCounter) {
        val body = deck.bodies[it.key]!!
        NewSound(
            type = dice.takeOne(listOf(Sounds.footStep1, Sounds.footStep2)),
            position = body.position,
            volume = 0.5f,
        )
      } else
        null
    }
