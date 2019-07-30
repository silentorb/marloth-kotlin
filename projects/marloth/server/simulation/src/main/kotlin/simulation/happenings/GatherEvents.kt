package simulation.happenings

import mythic.ent.Id
import mythic.ent.IdSource
import simulation.physics.old.Collision
import simulation.combat.Damage
import simulation.entities.*
import simulation.main.*
import simulation.misc.Definitions

typealias EventMapper<ActionType, EventType> = (ActionType, Id, Id, Int?) -> EventType

inline fun <reified ActionType, EventType: Any> gatherMappedEvents(crossinline mapper: EventMapper<ActionType, EventType>): (List<Triggering>) -> List<EventType> =
    { triggers ->
      triggers.mapNotNull { trigger ->
        val action = trigger.action
        val target = trigger.target
        val actor = trigger.actor
        val strength = trigger.strength
        when (action) {
          is ActionType -> mapper(action, target, actor, strength)
          else -> null
        }
      }
    }

val gatherDamageEvents = gatherMappedEvents<DamageAction, DamageEvent> { action, target, actor, strength ->
  DamageEvent(
      damage = Damage(
          type = action.damageType,
          amount = overTime(strength ?: action.amount),
          source = actor
      ),
      target = target
  )
}

val gatherTakeItemEvents = gatherMappedEvents<TakeItem, TakeItemEvent> { action, target, actor, strength ->
  TakeItemEvent(
      actor = actor,
      item = target
  )
}

typealias ActionHandler<T> = (Definitions, Deck, T, Id, Id) -> DeckSource

val applyBuff: ActionHandler<ApplyBuff> = { definitions, deck, action, target, source ->
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
              category = AttachmentTypeId.buff,
              source = source
          ),
          buff = Modifier(
              type = modifierType,
              strength = action.strength
          ),
          timer = Timer(
              duration = duration
          )
      )
      toDeck(nextId(), hand)
    }
  }
}

fun gatherNewRecords(definitions: Definitions, deck: Deck, triggers: List<Triggering>): List<DeckSource> {
  return triggers.mapNotNull { trigger ->
    val action = trigger.action
    val target = trigger.target
    val source = trigger.actor
    when (action) {
      is ApplyBuff -> applyBuff(definitions, deck, action, target, source)
      else -> null
    }
  }
}

data class Triggering(
    val actor: Id,
    val action: Action,
    val target: Id,
    val strength: Int? = null
)

fun gatherActivatedTriggers(deck: Deck, definitions: Definitions, collisions: List<Collision>): List<Triggering> {
  val attachmentTriggers = deck.triggers.mapNotNull { trigger ->
    val attachment = deck.attachments[trigger.key]
    if (attachment != null) {
      Triggering(
          actor = trigger.key,
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
            actor = trigger.key,
            action = trigger.value.action,
            target = collision.second
        )
      } else null
    } else null
  }
  val buffTriggers = deck.buffs
      .mapNotNull { (id, buff) ->
        val definition = definitions.modifiers[buff.type]!!
        val attachment = deck.attachments[id]!!
        val overTime = definition.overTime
        if (overTime != null)
          Triggering(
              actor = attachment.source,
              action = overTime,
              target = attachment.target,
              strength = buff.strength
          )
        else null
      }

  return attachmentTriggers.plus(sensorTriggers).plus(buffTriggers)
}

fun gatherEvents(definitions: Definitions, deck: Deck, triggers: List<Triggering>, events: Events): OrganizedEvents {
  return OrganizedEvents(
      damage = events.filterIsInstance<DamageEvent>().plus(gatherDamageEvents(triggers)),
      purchases = events.filterIsInstance<PurchaseEvent>(),
      takeItems = events.filterIsInstance<TakeItemEvent>().plus(gatherTakeItemEvents(triggers)),
      decks = events.filterIsInstance<DeckEvent>().map { it.deck }.plus(gatherNewRecords(definitions, deck, triggers))
  )
}
