package simulation.particles

import mythic.ent.Id
import mythic.ent.Table
import mythic.ent.pipe
import physics.Body
import randomly.Dice
import simulation.Depiction

typealias ParticleFactory = (Dice) -> ParticleHand
typealias ParticleUpdater = (Float, ParticleDeck) -> ParticleDeck

// Currently only one particle can be generated per frame and the simulation is capped at 60 frames per second
//  so the current maximum emission rate is 60 particles per second.  Higher rates can be set but will not act any
// different than if the rate were set to 60.
// If it ever becomes an issue, the emission code can be modified to generate multiple particles per frame.
// Until then, it's cleaner to have a max of one particle per frame.
data class Emitter(
    val particlesPerSecond: Float,
    val emit: ParticleFactory
)

data class ParticleHand(
    val body: Body,
    val depiction: Depiction,
    val life: Float
)

data class ParticleDeck(
    val bodies: Table<Body> = mapOf(),
    val depictions: Table<Depiction> = mapOf(),
    val life: Table<Float> = mapOf()
)

data class ParticleEffect(
    val emitter: Emitter,
    val updateParticles: ParticleUpdater,
    val deck: ParticleDeck,
    val accumulator: Float = 0f,
    val nextId: Id = 1L
)

fun addHand(deck: ParticleDeck, id: Id, hand: ParticleHand): ParticleDeck =
    deck.copy(
        bodies = deck.bodies.plus(Pair(id, hand.body)),
        depictions = deck.depictions.plus(Pair(id, hand.depiction)),
        life = deck.life.plus(Pair(id, hand.life))
    )

fun updateParticles(delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
  effect.copy(
      deck = effect.updateParticles(delta, effect.deck)
  )
}

fun updateParticleEffect(dice: Dice, delta: Float): (ParticleEffect) -> ParticleEffect = pipe(listOf(
    updateParticles(delta),
    updateParticleEmission(dice, delta)
))
