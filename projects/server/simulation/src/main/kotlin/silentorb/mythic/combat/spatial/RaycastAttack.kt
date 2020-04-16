package silentorb.mythic.combat.spatial

import silentorb.mythic.combat.general.WeaponDefinition
import silentorb.mythic.combat.general.newDamageEvents
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.castCollisionRay
import silentorb.mythic.spatial.Vector3

fun raycastAttack(world: SpatialCombatWorld, attacker: Id, weapon: WeaponDefinition, target: Vector3?): Events {
  val deck = world.deck
  val bulletState = world.bulletState
  val (origin, vector) = getAttackerOriginAndFacing(deck, attacker, target, 0.3f)
  val end = origin + vector * 30f
  val collision = castCollisionRay(bulletState.dynamicsWorld, origin, end)
  return if (collision != null && deck.destructibles.containsKey(collision.collisionObject)) {
    newDamageEvents(collision.collisionObject, attacker, weapon.damages, position = collision.hitPoint)
  } else
    listOf()
}
