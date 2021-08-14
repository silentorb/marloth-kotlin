package simulation.combat.spatial

import marloth.scenery.enums.Meshes
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.Collision
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.getCenter
import silentorb.mythic.spatial.minMax
import silentorb.mythic.timing.FloatTimer
import simulation.combat.general.DamageDefinition
import simulation.combat.general.newDamageEvents
import simulation.entities.CollisionMap
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.main.NewHand
import kotlin.math.pow
import kotlin.math.roundToInt

data class Missile(
    val damageRadius: Float, // 0f for no AOE
    val damageFalloff: Float, // Falloff Exponent
    val damages: List<DamageDefinition>
)

fun applyFalloff(fallOff: Float, range: Float, damages: List<DamageDefinition>, distance: Float): List<DamageDefinition> {
  val fallOffModifier = fallOff.pow(minMax(distance / range, 0f, 1f))
  return damages.map { damage ->
    damage.copy(
        amount = (damage.amount.toFloat() * fallOffModifier).roundToInt()
    )
  }
}

fun eventsFromMissileCollision(world: SpatialCombatWorld, id: Id, missile: Missile, collision: Collision): Events {
  val deck = world.deck
  val origin = deck.bodies[id]!!.position
  val damageRadius = missile.damageRadius
  val damageEvents = deck.bodies
      .minus(id)
      .filter { it.value.position.distance(origin) < damageRadius }
      .mapNotNull { (target, targetBody) ->
        val destructible = deck.destructibles[target]
        if (destructible != null) {
          val distance = targetBody.position.distance(origin)
          val damages = applyFalloff(missile.damageFalloff, damageRadius, missile.damages, distance)
          newDamageEvents(target, id, damages, position = getCenter(origin, targetBody.position))
        } else
          null
      }
      .flatten()
  val body = world.deck.bodies[id]!!
  val explosionDepiction = NewHand(
      components = listOf(
          Body(
              position = body.position,
              scale = Vector3(missile.damageRadius)
          ),
          Depiction(
              type = DepictionType.staticMesh,
              mesh = Meshes.sphere
          ),
          FloatTimer(0.15f)
      )
  )
  return listOf(DeleteEntityEvent(id)) + damageEvents + explosionDepiction
}

fun eventsFromMissiles(world: SpatialCombatWorld, collisions: CollisionMap): Events =
    world.deck.missiles
        .mapNotNull { (id, missile) ->
          val collision = collisions[id]
          if (collision != null) {
            eventsFromMissileCollision(world, id, missile, collision)
          } else
            null
        }
        .flatten()
