package simulation.happenings

import marloth.scenery.enums.Sounds
import silentorb.mythic.audio.NewSound
import silentorb.mythic.characters.rigs.defaultCharacterHeight
import silentorb.mythic.characters.rigs.isGrounded
import silentorb.mythic.ent.Table
import silentorb.mythic.happenings.Events
import silentorb.mythic.randomly.Dice
import simulation.characters.Character
import simulation.main.Deck
import kotlin.math.min

fun eventsFromCharacters(deck: Deck, previous: Table<Character>, dice: Dice): Events =
    deck.characters.mapNotNull {
      val previousCounter = previous[it.key]?.stepCounter ?: 0f
      if (it.value.stepCounter < previousCounter) {
        val body = deck.bodies[it.key]!!
        NewSound(
            type = dice.takeOne(listOf(Sounds.footStep1, Sounds.footStep2)),
            position = body.position - defaultCharacterHeight / 2f,
            volume = 0.5f,
        )
      } else
        null
    }

fun eventsFromCharacterRigs(deck: Deck, previousDeck: Deck): Events =
    deck.characterRigs.mapNotNull {(id, rig) ->
      val previous = previousDeck.characterRigs[id]
      val body = previousDeck.bodies[id]
      val minVelocity = 3f
      if (previous != null && body != null && !isGrounded(previous) && isGrounded(rig) && body.velocity.z < -minVelocity) {
        NewSound(
            type = Sounds.landOnFloor,
            position = body.position - defaultCharacterHeight / 2f,
            volume = 0.1f + 0.9f * min(1f, (-body.velocity.z - minVelocity) / 10f),
//        volume = 1f,
        )
      } else
        null
    }
