package simulation.evention

import simulation.physics.Collision
import simulation.combat.Damage
import simulation.main.*
import simulation.misc.*

fun gatherDamageEvents(deck: Deck, collisions: List<Collision>): List<DamageEvent> {
  return collisions.mapNotNull { collision ->
    val trigger = deck.triggers[collision.first]
    if (trigger == null || !(trigger.action is DamageAction))
      null
    else {
      val action = trigger.action
      DamageEvent(
          damage = Damage(
              type = action.damageType,
              amount = action.amount,
              source = collision.first
          ),
          target = collision.second
      )
    }
  }
}

fun gatherNewRecords(deck: Deck, collisions: List<Collision>): DeckSource {
  return { nextId ->
    val decks = collisions.mapNotNull { collision ->
      val trigger = deck.triggers[collision.first]
      if (trigger == null)
        null
      else {
        val action = trigger.action
        when (action) {
          is ApplyBuff -> {
            val target = collision.second
            val buffType = action.buffType
            val existing = getAttachmentOfEntityType(deck, target, buffType)
            if (existing != null)
              null
            else
              toDeck(nextId(), Hand(
                  attachment = Attachment(
                      target = target,
                      category = AttachmentTypeId.buff.name
                  ),
                  entity = Entity(
                      type = buffType
                  ),
                  timer = Timer(
                      duration = action.duration
                  )
              ))

          }
          else -> null
        }
      }
    }
    if (decks.none())
      null
    else
      mergeDecks(decks)
  }
}

fun gatherEvents(deck: Deck, collisions: List<Collision>): Events {
  return Events(
      damage = gatherDamageEvents(deck, collisions),
      deckSource = gatherNewRecords(deck, collisions)
  )
}
