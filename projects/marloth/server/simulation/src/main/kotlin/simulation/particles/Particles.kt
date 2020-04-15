package simulation.particles

import silentorb.mythic.ent.pipe2
import silentorb.mythic.particles.ParticleEffect
import silentorb.mythic.particles.ParticleEffectDefinitions
import silentorb.mythic.particles.updateParticleEmission
import silentorb.mythic.particles.updateParticles
import silentorb.mythic.physics.Body
import silentorb.mythic.randomly.Dice

fun updateParticleEffect(
    definitions: ParticleEffectDefinitions,
    dice: Dice,
    delta: Float
): (Body, ParticleEffect) -> ParticleEffect = { body, effect ->
  val definition = definitions[effect.type]!!
  pipe2(listOf(
      updateParticles(definition, delta),
      updateParticleEmission(definition, dice, body, delta)
  ))(effect)
}
