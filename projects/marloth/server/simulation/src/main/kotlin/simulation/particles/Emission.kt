package simulation.particles

import mythic.ent.IdSource
import randomly.Dice

fun getShouldEmit(dice: Dice, elapsedTime: Float, rate: Float): Boolean {
  val emissionChance = elapsedTime * rate
  return emissionChance >= 1f || dice.getFloat() <= emissionChance
}

fun updateParticleEmission(dice: Dice, delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
  val emitter = effect.emitter
  val deck = effect.deck
  val elapsedTime = effect.accumulator + delta
  val shouldEmit = getShouldEmit(dice, elapsedTime, emitter.particlesPerSecond)
  val newDeck = if (shouldEmit) {
    val hand = emitter.emit(dice)
    addHand(deck, effect.nextId, hand)
  } else
    effect.deck

  effect.copy(
      accumulator = if (shouldEmit) 0f else elapsedTime,
      deck = newDeck,
      nextId = if (shouldEmit) effect.nextId + 1 else effect.nextId
  )
}
