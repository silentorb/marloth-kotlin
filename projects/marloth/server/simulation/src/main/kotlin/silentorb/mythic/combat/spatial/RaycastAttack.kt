package silentorb.mythic.combat.spatial

import silentorb.mythic.audio.NewSound
import silentorb.mythic.combat.general.Damage
import silentorb.mythic.combat.general.DamageEvent
import silentorb.mythic.combat.general.newDamageEvents
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.castCollisionRay

fun raycastAttack(world: SpatialCombatWorld, event: AttackEvent): Events {
  val (definitions, deck, bulletState) = world
  val attacker = event.attacker
  val accessory = event.accessory
  val weapon = definitions.weapons[accessory]!!
  val (origin, vector) = getAttackerOriginAndFacing(deck, attacker)
  val end = origin + vector * 30f
  val collision = castCollisionRay(bulletState.dynamicsWorld, origin, end)
  return if (collision != null && deck.destructibles.containsKey(collision.collisionObject)) {
    newDamageEvents(collision.collisionObject, attacker, weapon.damages)
  } else
    listOf()
}
