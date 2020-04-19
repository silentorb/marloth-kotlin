package silentorb.mythic.combat.spatial

import silentorb.mythic.combat.general.DamageDefinition
import silentorb.mythic.combat.general.newDamageEvents
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Collision
import silentorb.mythic.spatial.getCenter
import simulation.entities.CollisionMap

data class Missile(
    val damageRadius: Float, // 0f for no AOE
    val damageFalloff: Float, // Falloff Exponent
    val damages: List<DamageDefinition>
)

fun eventsFromMissileCollision(world: SpatialCombatWorld, id: Id, missile: Missile, collision: Collision): Events {
  val deck = world.deck
  val origin = deck.bodies[id]!!.position
  val damageEvents = deck.bodies
      .minus(id)
      .filter { it.value.position.distance(origin) < missile.damageRadius }
      .mapNotNull { (target, targetBody) ->
        val destructible = deck.destructibles[target]
        if (destructible != null)
          newDamageEvents(target, id, missile.damages, position = getCenter(origin, targetBody.position))
        else
          null
      }
      .flatten()

  return listOf(DeleteEntityEvent(id)) + damageEvents
}

fun eventsFromMissiles(world: SpatialCombatWorld, collisions: CollisionMap): Events =
    world.deck.missiles
        .mapNotNull { (id, missile) ->
          val collision = collisions[id]
          if (collision != null && world.deck.collisionShapes[collision.second]!!.isSolid)
            eventsFromMissileCollision(world, id, missile, collision)
          else
            null
        }
        .flatten()
