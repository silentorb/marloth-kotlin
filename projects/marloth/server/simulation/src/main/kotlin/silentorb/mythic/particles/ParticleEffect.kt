package silentorb.mythic.particles

import silentorb.mythic.breeze.AnimationChannel
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3

typealias LifeRange = Pair<Float, Float>

//// Currently only one particle can be generated per frame and the simulation is capped at 60 frames per second
////  so the current maximum emission rate is 60 particles per second.  Higher rates can be set but will not act any
//// different than if the rate were set to 60.
//// If it ever becomes an issue, the emission code can be modified to generate multiple particles per frame.
//// Until then, it's cleaner to have a max of one particle per frame.
//data class Emitter(
//    val particlesPerSecond: Float,
//    val volume: Shape,
//    val life: LifeRange,
//    val initialVelocity: Vector3
//)

data class EmitterInput(
    val dice: Dice,
    val definition: ParticleEffectDefinition,
    val emitterPosition: Vector3,
    val timeElapsed: Float
)

typealias Emitter = (EmitterInput) -> List<Particle>

data class ParticleAnimation(
    val color: AnimationChannel? = null
)

typealias ParticleEffectName = String

data class ParticleEffectDefinition(
    val initialAppearance: ParticleAppearance,
    val animation: ParticleAnimation,
    val velocityMod: VelocityMod,
    val emitter: Emitter
)

typealias ParticleEffectDefinitions = Map<ParticleEffectName, ParticleEffectDefinition>

data class ParticleEffect(
    val type: ParticleEffectName,
    val particles: List<Particle> = listOf(),
    val timeElapsed: Float = 0f,
    val accumulator: Float = 0f
)

fun updateParticles(definition: ParticleEffectDefinition, delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
  effect.copy(
      particles = effect.particles.map(updateParticle(definition.animation, definition.velocityMod, delta))
          .filter { it.life < it.maxLife }
  )
}
