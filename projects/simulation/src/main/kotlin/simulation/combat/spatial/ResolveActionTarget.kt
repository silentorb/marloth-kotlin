package simulation.combat.spatial

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.firstRayHit
import simulation.combat.toSpatialCombatDeck
import simulation.main.World
import simulation.physics.CollisionGroups

fun resolveActionTarget(world: World, actor: Id, target: Id?): Id? =
    if (target != null)
      target
    else {
      val deck = world.deck
      val bulletState = world.bulletState
      val (origin, vector) = getAttackerOriginAndFacing(toSpatialCombatDeck(deck), actor, null, 0.3f)
      val end = origin + vector * 30f
      val collision = firstRayHit(bulletState.dynamicsWorld, origin, end, CollisionGroups.tangibleMask)
      val hitEntity = collision?.collisionObject as? Id?
      if (deck.characters.containsKey(hitEntity))
        hitEntity
      else
        null
    }

fun withResolvedTarget(world: World, actor: Id, target: Id?, transform: (Id) -> Events): Events {
  val resolvedTarget = resolveActionTarget(world, actor, target)
  return if (resolvedTarget != null)
    transform(resolvedTarget)
  else
    listOf()
}
