package silentorb.mythic.combat.spatial

import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.audio.NewSound
import silentorb.mythic.combat.general.Damage
import silentorb.mythic.combat.general.DamageEvent
import silentorb.mythic.combat.general.WeaponDefinition
import silentorb.mythic.combat.general.newDamageEvents
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.castCollisionRay

fun raycastAttack(world: SpatialCombatWorld, attacker: Id, weapon: WeaponDefinition): Events {
  val (definitions, deck, bulletState) = world
  val (origin, vector) = getAttackerOriginAndFacing(deck, attacker, 0.3f)
  val end = origin + vector * 30f
  val collision = castCollisionRay(bulletState.dynamicsWorld, origin, end)
  return if (collision != null && deck.destructibles.containsKey(collision.collisionObject)) {
    newDamageEvents(collision.collisionObject, attacker, weapon.damages)
  } else
    listOf()
}
