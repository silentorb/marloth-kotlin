package marloth.definition

import mythic.breeze.AnimationChannel
import mythic.breeze.Keyframe
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import physics.Body
import scenery.Cylinder
import simulation.Hand
import simulation.particles.Emitter
import simulation.particles.ParticleAnimation
import simulation.particles.ParticleAppearance
import simulation.particles.ParticleEffect

private fun newDamagedCloudParticleAnimation(baseColor: Vector3): ParticleAnimation {
  return ParticleAnimation(
      color = AnimationChannel(
          target = "color",
          keys = listOf(
              Keyframe(
                  time = 0f,
                  value = Vector4(baseColor, 0f)
              ),
              Keyframe(
                  time = 0.2f,
                  value = Vector4(baseColor, 0.33f)
              ),
              Keyframe(
                  time = 0.8f,
                  value = Vector4(baseColor, 0.33f)
              ),
              Keyframe(
                  time = 1f,
                  value = Vector4(baseColor, 0f)
              )
          )
      )
  )
}

fun newDamageCloud(position: Vector3, radius: Float): Hand {
  val baseColor = Vector3(0.5f, 1f, 0.5f)
  return Hand(
      body = Body(
          position = position
      ),
      particleEffect = ParticleEffect(
          initialAppearance = ParticleAppearance(
              texture = TextureId.perlinParticle.name,
              color = Vector4(baseColor, 0f),
              size = 2f
          ),
          animation = newDamagedCloudParticleAnimation(baseColor),
          emitter = Emitter(
              particlesPerSecond = 30f,
              volume = Cylinder(radius = radius, height = 10f),
              life = Pair(3f, 4f),
              initialVelocity = Vector3(0f, 0f, 1f)
          )
      )
  )
}
