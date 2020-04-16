package marloth.definition.templates

import silentorb.mythic.breeze.AnimationChannel
import silentorb.mythic.breeze.Keyframe
import silentorb.mythic.breeze.interpolateVector3
import silentorb.mythic.particles.ParticleAnimation
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.newVector4

fun newColorTransitionParticleAnimation(firstColor: Vector3, secondColor: Vector3, maxOpacity: Float): ParticleAnimation {
  return ParticleAnimation(
      color = AnimationChannel(
          target = "color",
          keys = listOf(
              Keyframe(
                  time = 0f,
                  value = newVector4(firstColor, 0f)
              ),
              Keyframe(
                  time = 0.2f,
                  value = newVector4(interpolateVector3(firstColor, secondColor, 0.2f), maxOpacity)
              ),
              Keyframe(
                  time = 0.8f,
                  value = newVector4(interpolateVector3(firstColor, secondColor, 0.8f), maxOpacity)
              ),
              Keyframe(
                  time = 1f,
                  value = newVector4(secondColor, 0f)
              )
          )
      )
  )
}
