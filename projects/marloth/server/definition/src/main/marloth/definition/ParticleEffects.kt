package marloth.definition

import simulation.evention.DamageAction
import simulation.evention.Trigger
import mythic.breeze.AnimationChannel
import mythic.breeze.Keyframe
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import simulation.physics.Body
import simulation.physics.CollisionObject
import scenery.Cylinder
import simulation.main.Hand
import simulation.combat.DamageType
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
  val shape = Cylinder(radius = radius, height = 10f)
  return Hand(
      body = Body(
          position = position
      ),
      collisionShape = CollisionObject(
          shape = shape,
          isSolid = false
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
              volume = shape,
              life = Pair(3f, 4f),
              initialVelocity = Vector3(0f, 0f, 0.7f)
          )
      ),
      trigger = Trigger(
          action = DamageAction(
              type = DamageType.poison,
              amount = 1
          )
      )
  )
}
