package silentorb.mythic.particles

import silentorb.mythic.breeze.interpolateKeys
import silentorb.mythic.breeze.interpolateVector4
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.scenery.TextureName

typealias VelocityMod = (Vector3) -> Vector3

data class ParticleAppearance(
    val texture: TextureName,
    val color: Vector4,
    val size: Float
)

data class Particle(
    val texture: TextureName,
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

fun updateParticle(animation: ParticleAnimation, velocityMod: VelocityMod, delta: Float): (Particle) -> Particle = { particle ->
  val life = particle.life + delta
  val progress = life / particle.maxLife
  val color = if (animation.color == null)
    particle.color
  else
    interpolateKeys(animation.color.keys, progress, interpolateVector4)

  particle.copy(
      life = life,
//      velocity = velocityMod(particle.velocity),
      position = particle.position + particle.velocity * delta,
      animationStep = (particle.animationStep + delta * 0.3f) % 1f,
      color = color
  )
}
