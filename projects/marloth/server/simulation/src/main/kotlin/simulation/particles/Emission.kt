package simulation.particles

import mythic.spatial.Pi
import mythic.spatial.Vector3
import mythic.spatial.projectVector3
import physics.Body
import randomly.Dice
import scenery.Cylinder
import scenery.Shape

fun getShouldEmit(dice: Dice, elapsedTime: Float, rate: Float): Boolean {
  val emissionChance = elapsedTime * rate
  return emissionChance >= 1f || dice.getFloat() <= emissionChance
}

fun placeParticle(dice: Dice, shape: Shape): Vector3 =
    when (shape) {
      is Cylinder -> projectVector3(
          dice.getFloat(Pi * 2f),
          dice.getFloat(shape.radius),
          dice.getFloat(shape.height) - shape.height / 2f
      )
      else -> throw Error("Not supported")
    }

fun emitParticle(dice: Dice, effect: ParticleEffect, emitterPosition: Vector3): Particle {
  val emitter = effect.emitter
  val position = emitterPosition + placeParticle(dice, emitter.volume)
  val velocity = emitter.initialVelocity
  val life = dice.getFloat(emitter.life.first, emitter.life.second)
  return newParticle(effect.initialAppearance, position, velocity, life)
}

fun updateParticleEmission(dice: Dice, body: Body, delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
  val emitter = effect.emitter
  val elapsedTime = effect.accumulator + delta
  val shouldEmit = getShouldEmit(dice, elapsedTime, emitter.particlesPerSecond)
  val newParticles = if (shouldEmit) {
    effect.particles.plus(emitParticle(dice, effect, body.position))
  } else
    effect.particles

  // I'm not sure but setting accumulator straight to 0 may be throwing away some precision, but for at least
  // initial emission use cases this shouldn't be a problem.
  val newAccumulator = if (shouldEmit) 0f else elapsedTime
  effect.copy(
      accumulator = newAccumulator,
      particles = newParticles
  )
}
