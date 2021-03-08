package simulation.combat.spatial

import marloth.scenery.enums.MeshId
import simulation.combat.general.DamageDefinition
import simulation.combat.general.newDamageEvents
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.physics.Collision
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.getCenter
import silentorb.mythic.timing.FloatTimer
import simulation.entities.CollisionMap
import simulation.entities.Depiction
import simulation.entities.DepictionType
import simulation.happenings.NewHandEvent
import simulation.main.Hand

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
  val body = world.deck.bodies[id]!!
  val explosionDepiction = NewHandEvent(
      hand = Hand(
          body = Body(
              position = body.position,
              scale = Vector3(missile.damageRadius)
          ),
          depiction = Depiction(
              type = DepictionType.staticMesh,
              mesh = MeshId.sphere
          ),
          timerFloat = FloatTimer(0.15f)
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
