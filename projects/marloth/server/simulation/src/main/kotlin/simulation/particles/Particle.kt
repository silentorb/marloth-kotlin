package simulation.particles

import mythic.breeze.getStandardChannelValue
import mythic.breeze.interpolateKeys
import mythic.breeze.interpolateVector4
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import scenery.TextureId

data class ParticleAppearance(
    val texture: TextureId,
    val color: Vector4,
    val size: Float
)

data class Particle(
    val texture: TextureId,
    val color: Vector4,
    val size: Float,
    val position: Vector3,
    val velocity: Vector3,
    val maxLife: Float,
    val life: Float = 0f,
    val animationStep: Float = 0f
)

fun newParticle(appearance: ParticleAppearance, position: Vector3, velocity: Vector3, life: Float) =
    Particle(
        texture = appearance.texture,
        color = appearance.color,
        size = appearance.size,
        maxLife = life,
        position = position,
        velocity = velocity
    )

fun updateParticle(animation: ParticleAnimation, delta: Float): (Particle) -> Particle = { particle ->
  val life = particle.life + delta
  val progress = life / particle.maxLife
  val color = if (animation.color == null)
    particle.color
  else
    interpolateKeys(animation.color.keys, progress, interpolateVector4)

  particle.copy(
      life = life,
      position = particle.position + particle.velocity * delta,
      animationStep = (particle.animationStep + delta * 0.5f) % 1f,
      color = color
  )
}
