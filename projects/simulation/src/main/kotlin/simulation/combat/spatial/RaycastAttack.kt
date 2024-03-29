package simulation.combat.spatial

import simulation.combat.general.WeaponDefinition
import simulation.combat.general.newDamageEvents
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.LinearImpulse
import silentorb.mythic.physics.firstRayHit
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.spatial.minMax
import simulation.physics.CollisionGroups

fun raycastAttack(world: SpatialCombatWorld, attacker: Id, weapon: WeaponDefinition, targetLocation: Vector3?, targetEntity: Id?): Events {
  val deck = world.deck
  val bulletState = world.bulletState
  val (origin, vector) = getAttackerOriginAndFacing(deck, attacker, targetLocation, targetEntity, 0.3f)
  val end = origin + vector * 30f
  val collision = firstRayHit(bulletState.dynamicsWorld, origin, end, CollisionGroups.tangibleMask)
  val collisionObject = collision?.collisionObject as? Id?
  return if (collisionObject != null && deck.destructibles.containsKey(collisionObject)) {
    val damageEvents = newDamageEvents(collisionObject, attacker, weapon.damages, position = collision.hitPoint)
    if (weapon.impulse != 0f) {
      val distance = origin.distance(collision.hitPoint) - 1.5f
      val maxImpulseRange = 10f
      val impulse = weapon.impulse * (1f - minMax(distance / maxImpulseRange, 0f, 1f))
      damageEvents + LinearImpulse(
          body = collisionObject,
          offset = (vector + Vector3(0f, 0f, 0.3f)).normalize() * impulse
      )
    } else
      damageEvents
  } else
    listOf()
}
