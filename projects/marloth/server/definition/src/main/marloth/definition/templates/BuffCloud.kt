package marloth.definition.templates

import marloth.scenery.enums.ModifierId
import marloth.scenery.enums.TextureId
import simulation.happenings.Trigger
import silentorb.mythic.breeze.AnimationChannel
import silentorb.mythic.breeze.Keyframe
import silentorb.mythic.breeze.interpolateVector3
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.Vector4
import silentorb.mythic.physics.Body
import simulation.physics.CollisionObject
import silentorb.mythic.scenery.Cylinder
import simulation.main.Hand
import simulation.happenings.ApplyBuff
import simulation.particles.Emitter
import simulation.particles.ParticleAnimation
import simulation.particles.ParticleAppearance
import simulation.particles.ParticleEffect

private val cloudColors: Map<ModifierId, Pair<Vector3, Vector3>> = mapOf(
    ModifierId.damageBurning to Pair(Vector3(1f, 0.7f, 0f), Vector3(1f, 0.3f, 0.1f)),
    ModifierId.damageChilled to Pair(Vector3(0.2f, 0.3f, 1f), Vector3(0.9f, 0.9f, 1f)),
    ModifierId.damagePoisoned to Pair(Vector3(0.4f, 1f, 0.5f), Vector3(0.8f, 0.9f, 0.5f))
)

private fun newDamageCloudParticleAnimation(firstColor: Vector3, secondColor: Vector3): ParticleAnimation {
  val maxOpacity = 0.33f
  return ParticleAnimation(
      color = AnimationChannel(
          target = "color",
          keys = listOf(
              Keyframe(
                  time = 0f,
                  value = Vector4(firstColor, 0f)
              ),
              Keyframe(
                  time = 0.2f,
                  value = Vector4(interpolateVector3(firstColor, secondColor, 0.2f), maxOpacity)
              ),
              Keyframe(
                  time = 0.8f,
                  value = Vector4(interpolateVector3(firstColor, secondColor, 0.8f), maxOpacity)
              ),
              Keyframe(
                  time = 1f,
                  value = Vector4(secondColor, 0f)
              )
          )
      )
  )
}

fun newBuffCloud(position: Vector3, radius: Float, buff: ModifierId): Hand {
  val colors = cloudColors[buff]!!
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
              color = Vector4(colors.first, 0f),
              size = 2f
          ),
          animation = newDamageCloudParticleAnimation(colors.first, colors.second),
          emitter = Emitter(
              particlesPerSecond = 30f,
              volume = shape,
              life = Pair(3f, 5f),
              initialVelocity = Vector3(0f, 0f, 0.7f)
          )
      ),
      trigger = Trigger(
          action = ApplyBuff(
              buffType = buff,
              strength = 10,
              duration = 4
          )
      )
  )
}
