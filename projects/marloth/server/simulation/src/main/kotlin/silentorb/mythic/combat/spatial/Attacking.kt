package silentorb.mythic.combat.spatial

import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.audio.NewSound
import silentorb.mythic.combat.general.Damage
import silentorb.mythic.combat.general.DamageEvent
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.happenings.UseAction
import silentorb.mythic.physics.BulletState
import silentorb.mythic.physics.castCollisionRay
import silentorb.mythic.spatial.Vector3

const val attackMarker = "attack"

data class RaycastAttack(
    val attacker: Id,
    val accessory: AccessoryName
) : GameEvent

fun raycastAttack(world: SpatialCombatWorld): (RaycastAttack) -> Events = { event ->
  val (definitions, deck, bulletState) = world
  val attacker = event.attacker
  val accessory = event.accessory
  val weapon = definitions.weapons[accessory]!!
  val body = deck.bodies[attacker]!!
  val characterRig = deck.characterRigs[attacker]!!
  val vector = characterRig.facingVector
  val origin = body.position + Vector3(0f, 0f, 0.2f) + vector * 0.3f
  val end = origin + vector * 30f
  val collision = castCollisionRay(bulletState.dynamicsWorld, origin, end)
  val hitEvents = if (collision != null && deck.destructibles.containsKey(collision.collisionObject)) {
    weapon.damages.map { damage ->
      DamageEvent(
          target = collision.collisionObject,
          damage = Damage(
              type = damage.type,
              amount = damage.amount,
              source = attacker
          )
      )
    }
  } else
    listOf()

  if (weapon.sound != null)
    hitEvents
        .plus(listOf(
            NewSound(
                type = weapon.sound,
                volume = 1f,
                position = origin
            )
        ))
  else
    hitEvents
}

fun startRaycastAttack(attacker: Id, action: Id, accessory: AccessoryName): Events {
  return listOf(
      UseAction(
          actor = attacker,
          action = action,
          deferredEvents = mapOf(
              attackMarker to RaycastAttack(
                  attacker = attacker,
                  accessory = accessory
              )
          )
      )
  )
}
