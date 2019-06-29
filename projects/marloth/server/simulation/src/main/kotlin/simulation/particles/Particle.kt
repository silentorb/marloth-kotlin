package simulation.particles

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
    val life: Float,
    val animationStep: Float = 0f
)

fun newParticle(appearance: ParticleAppearance, position: Vector3, velocity: Vector3, life: Float) =
    Particle(
        texture = appearance.texture,
        color = appearance.color,
        size = appearance.size,
        life = life,
        position = position,
        velocity = velocity
    )

fun updateParticle(delta: Float): (Particle) -> Particle = { particle ->
  particle.copy(
      life = particle.life - delta,
      position = particle.position + particle.velocity * delta
  )
}
