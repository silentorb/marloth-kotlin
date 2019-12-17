package simulation.combat

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3
import simulation.happenings.DamageEvent
import silentorb.mythic.happenings.Events
import simulation.happenings.UseAction
import simulation.main.World
import silentorb.mythic.physics.castCollisionRay

fun raycastAttack(world: World, attacker: Id, action: Id): Events {
  val deck = world.deck
  val body = deck.bodies[attacker]!!
  val characterRig = deck.characterRigs[attacker]!!
  val vector = characterRig.facingVector
  val origin = body.position + Vector3(0f, 0f, 0.2f) + vector * 0.3f
  val end = origin + vector * 30f
  val collision = castCollisionRay(world.bulletState.dynamicsWorld, origin, end)
  return if (collision != null && deck.destructibles.containsKey(collision.collisionObject)) {
    listOf(
        DamageEvent(
            target = collision.collisionObject,
            damage = Damage(
                type = DamageType.fire,
                amount = 10,
                source = attacker
            )
        ),
        UseAction(
            actor = attacker,
            action = action
        )
    )
  } else
    listOf()
}
