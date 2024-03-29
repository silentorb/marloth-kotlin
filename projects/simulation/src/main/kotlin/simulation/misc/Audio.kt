package simulation.misc

import silentorb.mythic.aura.Sound
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.WorldPair

fun handsFromSounds(sounds: List<Sound>) =
    sounds.map { sound ->
      Hand(
          sound = sound
      )
    }

val deathSounds: (worlds: WorldPair) -> List<Sound> = { worlds ->
  worlds.second.deck.characters.filter { (key, value) ->
    val previous = worlds.first.deck.characters[key]
    previous != null && previous.isAlive && !value.isAlive
  }
      .mapNotNull { (actor, character) ->
        val characterDefinition = character.definition
        val type = characterDefinition.deathSound
        if (type != null) {
          val body = worlds.second.deck.bodies[actor]!!
          Sound(
              type = type,
              position = body.position,
              volume = 1f,
              progress = 0f
          )
        } else
          null
      }
}

val newAmbientSounds: (Deck, Deck) -> List<Hand> = { previous, next ->
  next.ambientSounds.filter { (id, emitter) ->
    emitter.sound != null && previous.ambientSounds[id]?.sound == null
  }
      .map { (id, emitter) ->
        val body = next.bodies[id]!!
        Hand(
            sound = Sound(
                type = emitter.sound!!,
                position = body.position,
                volume = 1f,
                progress = 0f
            )
        )
      }
}
