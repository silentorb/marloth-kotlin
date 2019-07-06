package simulation.evention

import simulation.physics.Collision
import simulation.Deck
import simulation.Events
import simulation.combat.Damage

fun gatherDamageEvents(deck: Deck, collisions: List<Collision>): List<DamageEvent> {
  return collisions.mapNotNull { collision ->
    val trigger = deck.triggers[collision.first]
    if (trigger == null || !(trigger.action is DamageAction))
      null
    else {
      val action = trigger.action
      DamageEvent(
          damage = Damage(
              type = action.type,
              amount = action.amount,
              source = collision.first
          ),
          target = collision.second
      )
    }
  }
}

fun gatherEvents(deck: Deck, collisions: List<Collision>): Events {
  return Events(
      damage = gatherDamageEvents(deck, collisions)
  )
}
