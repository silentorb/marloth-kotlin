package simulation.happenings

import mythic.ent.Id
import mythic.ent.IdSource
import simulation.physics.Collision
import simulation.combat.Damage
import simulation.entities.*
import simulation.main.*
import simulation.misc.Definitions

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

typealias ActionHandler = (Definitions, Deck, ApplyBuff, Id) -> DeckSource

val applyBuff: ActionHandler = { definitions, deck, action, target ->
  { nextId: IdSource ->
    val modifierType = action.buffType
    val duration = action.duration
    val existing = getAttachmentOfEntityType(deck, target, modifierType)
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
              category = AttachmentTypeId.buff
          ),
          buff = Modifier(
              type = modifierType,
              strength = action.strength
          ),
          timer = Timer(
              duration = duration
          )
      )
//            val template = templates[modifierType]
      toDeck(nextId(), hand)
//            if (template != null)
//              handDeck.plus(toDeck(id, template(action)))
//            else
//              handDeck
    }
  }
}

fun gatherNewRecords(definitions: Definitions, deck: Deck, triggers: List<Triggering>): List<DeckSource> {
  return triggers.mapNotNull { trigger ->
    val action = trigger.action
    val target = trigger.target
    val triggerKey = trigger.triggerKey
    when (action) {
      is ApplyBuff -> {
        applyBuff(definitions, deck, action, target)
      }
      else -> null
    }
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

fun gatherEvents(definitions: Definitions, deck: Deck, collisions: List<Collision>, events: Events): OrganizedEvents {
  val triggers = gatherActivatedTriggers(deck, collisions)

  return OrganizedEvents(
      damage = events.filterIsInstance<DamageEvent>().plus(gatherDamageEvents(deck, triggers)),
      purchases = events.filterIsInstance<PurchaseEvent>(),
      decks = events.filterIsInstance<DeckEvent>().map { it.deck }.plus(gatherNewRecords(definitions, deck, triggers))
  )
}
