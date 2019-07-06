package simulation.misc

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.Table
import randomly.Dice
import scenery.Sounds
import simulation.main.Deck
import simulation.main.simulationDelta

data class AmbientAudioEmitter(
    override val id: Id,
    val delay: Double,
    val sound: Sounds? = null
) : Entity

fun updateAmbientAudioEmitter(dice: Dice, deck: Deck): (AmbientAudioEmitter) -> AmbientAudioEmitter = { emitter ->
  val delay = emitter.delay - simulationDelta
  val newSound = delay <= 0.0
  val sound = if (newSound) {
    val character = deck.characters[emitter.id]!!
    dice.getItem(character.definition.ambientSounds)
  } else
    null

  val newDelay = if (newSound)
    dice.getFloat(1f, 3f).toDouble()
  else
    delay

  emitter.copy(
      delay = newDelay,
      sound = sound
  )
}

fun updateAmbientAudio(dice: Dice, deck: Deck): Table<AmbientAudioEmitter> =
    deck.ambientSounds
        .filter {
          val character = deck.characters[it.key]
          character != null && character.isAlive
        }
        .mapValues { (_, value) -> updateAmbientAudioEmitter(dice, deck)(value) }
