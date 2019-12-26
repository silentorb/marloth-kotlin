package simulation.combat

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.castCollisionRay
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.combat.Damage
import silentorb.mythic.combat.DamageEvent
import silentorb.mythic.happenings.UseAction
import simulation.main.World

fun raycastAttack(world: World, attacker: Id, action: Id, accessory: AccessoryName): Events {
  val deck = world.deck
  val definitions = world.definitions
  val weapon = definitions.weapons[accessory]!!
  val body = deck.bodies[attacker]!!
  val characterRig = deck.characterRigs[attacker]!!
  val vector = characterRig.facingVector
  val origin = body.position + Vector3(0f, 0f, 0.2f) + vector * 0.3f
  val end = origin + vector * 30f
  val collision = castCollisionRay(world.bulletState.dynamicsWorld, origin, end)
  return if (collision != null && deck.destructibles.containsKey(collision.collisionObject)) {
    listOf(
        UseAction(
            actor = attacker,
            action = action
        )
    )
        .plus(
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
        )
  } else
    listOf()
}
