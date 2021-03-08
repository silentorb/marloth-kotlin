package simulation.combat.spatial

import simulation.accessorize.AccessoryName
import simulation.combat.general.WeaponDefinition
import simulation.combat.general.newDamageEvents
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.spatial.Vector3

fun meleeAttack(world: SpatialCombatWorld, attacker: Id, weapon: WeaponDefinition, target: Vector3?,
                accessory: AccessoryName): Events {
  val deck = world.deck
  val shape = deck.collisionShapes[attacker]!!.shape
  val range = world.definitions.actions[accessory]!!.range
  val forwardOffset = range
  val (origin, vector) = getAttackerOriginAndFacing(deck, attacker, target, forwardOffset)
  return deck.destructibles.keys
      .minus(attacker)
      .mapNotNull { destructible ->
        val otherBody = deck.bodies[destructible]
        val otherShape = deck.collisionShapes[destructible]?.shape
        if (otherBody == null || otherShape == null)
          null
        else {
          val gap = origin.distance(otherBody.position) - otherShape.radius
          if (gap < range) {
            val otherVector = (otherBody.position - origin).normalize()
            vector.dot(otherVector) > 0.7f
            val hitPosition = origin// + vector * range
            newDamageEvents(destructible, attacker, weapon.damages, position = hitPosition)
          } else
            null
        }
      }
      .flatten()
}
