package marloth.definition.templates

import marloth.scenery.enums.AccessoryIdOld
import simulation.accessorize.AccessoryName
import silentorb.mythic.spatial.Vector3
import simulation.main.Hand

private val cloudColors: Map<AccessoryName, Pair<Vector3, Vector3>> = mapOf(
    AccessoryIdOld.damageBurning to Pair(Vector3(1f, 0.7f, 0f), Vector3(1f, 0.3f, 0.1f)),
    AccessoryIdOld.damageChilled to Pair(Vector3(0.2f, 0.3f, 1f), Vector3(0.9f, 0.9f, 1f)),
    AccessoryIdOld.damagePoisoned to Pair(Vector3(0.4f, 1f, 0.5f), Vector3(0.8f, 0.9f, 0.5f))
)

fun newBuffCloud(position: Vector3, radius: Float, buff: AccessoryName): Hand {
  throw Error("No longer implemented")
//  val colors = cloudColors[buff]!!
//  val shape = Cylinder(radius = radius, height = 10f)
//  return Hand(
//      body = Body(
//          position = position
//      ),
//      collisionShape = CollisionObject(
//          shape = shape,
//          isSolid = false
//      ),
//      particleEffect = ParticleEffect(
//          initialAppearance = ParticleAppearance(
//              texture = TextureId.perlinParticle,
//              color = newVector4(colors.first, 0f),
//              size = 2f
//          ),
//          animation = newColorTransitionParticleAnimation(colors.first, colors.second, 0.33f),
//          emitter = Emitter(
//              particlesPerSecond = 30f,
//              volume = shape,
//              life = Pair(3f, 5f),
//              initialVelocity = Vector3(0f, 0f, 0.7f)
//          )
//      ),
//      trigger = Trigger(
//          action = ApplyBuff(
//              buffType = buff,
//              strength = 10,
//              duration = 4
//          )
//      )
//  )
}
