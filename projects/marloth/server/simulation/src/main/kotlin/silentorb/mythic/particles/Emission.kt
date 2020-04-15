package silentorb.mythic.particles

import silentorb.mythic.physics.Body
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.Cylinder
import silentorb.mythic.scenery.Shape
import silentorb.mythic.scenery.Sphere
import silentorb.mythic.spatial.Pi
import silentorb.mythic.spatial.Quaternion
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.projectVector3

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
      is Sphere -> Quaternion()
          .rotateZ(dice.getFloat(Pi * 2f))
          .rotateY(dice.getFloat(Pi * 2f))
          .transform(Vector3(dice.getFloat(shape.radius), 0f, 0f))
      else -> throw Error("Not supported")
    }

//fun emitParticle(dice: Dice, definition: ParticleEffectDefinition, emitterPosition: Vector3): Particle {
//  val emitter = definition.emitter
//  val position = emitterPosition + placeParticle(dice, emitter.volume)
//  val velocity = emitter.initialVelocity
//  val life = dice.getFloat(emitter.life.first, emitter.life.second)
//  return newParticle(definition.initialAppearance, position, velocity, life)
//}
//
//fun updateParticleEmission(definition: ParticleEffectDefinition, dice: Dice, body: Body, delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
//  val emitter = definition.emitter
//  val elapsedTime = effect.accumulator + delta
//  val shouldEmit = getShouldEmit(dice, elapsedTime, emitter.particlesPerSecond)
//  val newParticles = if (shouldEmit) {
//    effect.particles.plus(emitParticle(dice, definition, body.position))
//  } else
//    effect.particles
//
//  // I'm not sure but setting accumulator straight to 0 may be throwing away some precision, but for at least
//  // initial emission use cases this shouldn't be a problem.
//  val newAccumulator = if (shouldEmit) 0f else elapsedTime
//  effect.copy(
//      accumulator = newAccumulator,
//      particles = newParticles
//  )
//}

typealias VelocitySource = (Dice) -> Vector3

data class CommonEmission(
    val volume: Shape,
    val life: LifeRange,
    val initialVelocity: VelocitySource
)

fun emitParticle(input: EmitterInput, emission: CommonEmission): Particle {
  val dice = input.dice
  val definition = input.definition
  val emitterPosition = input.emitterPosition

  val position = emitterPosition + placeParticle(dice, emission.volume)
  val velocity = emission.initialVelocity(dice)
  val life = dice.getFloat(emission.life.first, emission.life.second)
  return newParticle(definition.initialAppearance, position, velocity, life)
}

fun immediateEmmiter(particleCount: Int, emission: CommonEmission): Emitter = { input ->
  if (input.timeElapsed == 0f)
    (0 until particleCount).map { emitParticle(input, emission) }
  else
    listOf()
}

fun updateParticleEmission(definition: ParticleEffectDefinition, dice: Dice, body: Body, delta: Float): (ParticleEffect) -> ParticleEffect = { effect ->
  val emitterInput = EmitterInput(
      dice = dice,
      definition = definition,
      emitterPosition = body.position,
      timeElapsed = effect.timeElapsed
  )
  val newParticles = definition.emitter(emitterInput)

  effect.copy(
      timeElapsed = effect.timeElapsed + delta,
      particles = effect.particles + newParticles
  )
}
