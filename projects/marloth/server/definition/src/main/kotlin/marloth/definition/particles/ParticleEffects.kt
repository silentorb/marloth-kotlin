package marloth.definition.particles

import marloth.scenery.enums.ParticleEffectType
import silentorb.mythic.particles.ParticleEffectDefinitions

fun particleEffects(): ParticleEffectDefinitions = mapOf(
    ParticleEffectType.blood.name to bloodParticleEffect()
)
