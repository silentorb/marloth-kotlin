package marloth.definition.particles

import marloth.definition.templates.newColorTransitionParticleAnimation
import marloth.scenery.enums.Textures
import silentorb.mythic.particles.*
import silentorb.mythic.scenery.Sphere
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.newVector4

fun descendingVelocity(horizontalRange: Float, gravity: Float): VelocitySource =
    { dice ->
      Vector3(
          dice.getFloat(-horizontalRange, horizontalRange),
          dice.getFloat(-horizontalRange, horizontalRange),
          gravity
      )
    }

fun velocityGravityMod(gravity: Float, rate: Float): VelocityMod =
    { velocity ->
      velocity * (1f - rate) + gravity * rate
    }

fun bloodParticleEffect(): ParticleEffectDefinition {
  val firstColor = Vector3(0f, 0f, 0f)
//  val firstColor = Vector3(1f, 0f, 0f)
  val secondColor = firstColor * 0.5f
  val gravity = -1.1f
  return ParticleEffectDefinition(
      initialAppearance = ParticleAppearance(
          texture = Textures.perlinParticle,
          color = newVector4(firstColor, 0f),
          size = 0.09f
      ),
      animation = newColorTransitionParticleAnimation(firstColor, secondColor, 1f),
      velocityMod = velocityGravityMod(gravity, 0.1f),
      emitter = immediateEmmiter(16, CommonEmission(
          volume = Sphere(radius = 0.5f),
          life = Pair(0.5f, 0.9f),
          initialVelocity = descendingVelocity(0.5f, gravity)
      ))
  )
}
