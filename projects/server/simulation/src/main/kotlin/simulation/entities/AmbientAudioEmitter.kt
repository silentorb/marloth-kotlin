package simulation.entities

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.randomly.Dice
import marloth.scenery.enums.Sounds
import silentorb.mythic.aura.SoundType
import simulation.main.Deck
import simulation.updating.simulationDelta

data class AmbientAudioEmitter(
    val delay: Double,
    val sound: SoundType? = null
)

fun updateAmbientAudioEmitter(dice: Dice, deck: Deck): (Id, AmbientAudioEmitter) -> AmbientAudioEmitter = { id, emitter ->
  val delay = emitter.delay - simulationDelta
  val newSound = delay <= 0.0
  val sound = if (newSound) {
    val character = deck.characters[id]!!
    dice.takeOne(character.definition.ambientSounds)
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
        .mapValues { (id, value) -> updateAmbientAudioEmitter(dice, deck)(id, value) }
