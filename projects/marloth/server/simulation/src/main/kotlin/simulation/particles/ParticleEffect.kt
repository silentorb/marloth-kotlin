package simulation.particles

import mythic.breeze.AnimationChannel
import mythic.ent.pipe
import mythic.spatial.Vector3
import simulation.physics.Body
import randomly.Dice
import scenery.Shape

//typealias ParticleFactory = (Dice) -> ParticleHand
//typealias ParticleUpdater = (Float, ParticleDeck) -> ParticleDeck
typealias LifeRange = Pair<Float, Float>

// Currently only one particle can be generated per frame and the simulation is capped at 60 frames per second
//  so the current maximum emission rate is 60 particles per second.  Higher rates can be set but will not act any
// different than if the rate were set to 60.
// If it ever becomes an issue, the emission code can be modified to generate multiple particles per frame.
// Until then, it's cleaner to have a max of one particle per frame.
data class Emitter(
    val particlesPerSecond: Float,
    val volume: Shape,
    val life: LifeRange,
    val initialVelocity: Vector3
)

data class ParticleAnimation(
    val color: AnimationChannel? = null
)

data class ParticleEffect(
    val initialAppearance: ParticleAppearance,
    val animation: ParticleAnimation,
    val emitter: Emitter,
    val particles: List<Particle> = listOf(),
    val accumulator: Float = 0f
)

fun updateParticles(delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
  effect.copy(
      particles = effect.particles.map(updateParticle(effect.animation, delta))
          .filter { it.life < it.maxLife }
  )
}

fun updateParticleEffect(dice: Dice, delta: Float): (Body, ParticleEffect) -> ParticleEffect = { body, effect ->
  pipe(listOf(
      updateParticles(delta),
      updateParticleEmission(dice, body, delta)
  ))(effect)
}
