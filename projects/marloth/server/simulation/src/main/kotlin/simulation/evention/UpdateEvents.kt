package simulation.evention

import mythic.ent.Id
import simulation.physics.Collision
import simulation.combat.Damage
import simulation.main.*
import simulation.misc.*

fun gatherDamageEvents(deck: Deck, triggers: List<Triggering>): List<DamageEvent> {
  return triggers.mapNotNull { trigger ->
    val action = trigger.action
    val target = trigger.target
    val triggerKey = trigger.triggerKey
    when (action) {
      is DamageAction ->
        DamageEvent(
            damage = Damage(
                type = action.damageType,
                amount = overTime(action.amount),
                source = triggerKey
            ),
            target = target
        )
      else -> null
    }
  }
}

fun gatherNewRecords(templates: HandTemplates, deck: Deck, triggers: List<Triggering>): DeckSource {
  return { nextId ->
    val decks = triggers.mapNotNull { trigger ->
      val action = trigger.action
      val target = trigger.target
      val triggerKey = trigger.triggerKey
      when (action) {
        is ApplyBuff -> {
          val buffType = action.buffType
          val duration = action.duration
          val existing = getAttachmentOfEntityType(deck, target, buffType)
          if (existing != null)
            Deck(
                timers = mapOf(
                    existing to Timer(
                        duration = duration
                    )
                )
            )
          else {
            val hand = Hand(
                attachment = Attachment(
                    target = target,
                    category = AttachmentTypeId.buff.name
                ),
                buff = Buff(
                    strength = action.strength
                ),
                entity = Entity(
                    type = buffType
                ),
                timer = Timer(
                    duration = duration
                )
            )
            val template = templates[buffType]
            val id = nextId()
            val handDeck = toDeck(id, hand)
            if (template != null)
              handDeck.plus(toDeck(id, template(action)))
            else
              handDeck
          }
        }
        else -> null
      }
    }
    if (decks.none())
      null
    else
      mergeDecks(decks)
  }
}

data class Triggering(
    val triggerKey: Id,
    val action: Action,
    val target: Id
)

fun gatherActivatedTriggers(deck: Deck, collisions: List<Collision>): List<Triggering> {
  val attachmentTriggers = deck.triggers.mapNotNull { trigger ->
    val attachment = deck.attachments[trigger.key]
    if (attachment != null) {
      Triggering(
          triggerKey = trigger.key,
          action = trigger.value.action,
          target = attachment.target
      )
    } else
      null
  }
  val sensorTriggers = deck.triggers.mapNotNull { trigger ->
    if (deck.collisionShapes.containsKey(trigger.key)) {
      val collision = collisions.firstOrNull { it.first == trigger.key }
      if (collision != null) {
        Triggering(
            triggerKey = trigger.key,
            action = trigger.value.action,
            target = collision.second
        )
      } else null
    } else null
  }
  return attachmentTriggers.plus(sensorTriggers)
}

fun gatherEvents(templates: HandTemplates, deck: Deck, collisions: List<Collision>): Events {
  val triggers = gatherActivatedTriggers(deck, collisions)

  return Events(
      damage = gatherDamageEvents(deck, triggers),
      deckSource = gatherNewRecords(templates, deck, triggers)
  )
}
