package simulation.happenings

import silentorb.mythic.ent.Id
import simulation.combat.Damage
import silentorb.mythic.commanding.CommonCharacterCommands
import silentorb.mythic.commanding.Commands
import simulation.main.Deck
import simulation.main.DeckSource
import simulation.main.overTime
import simulation.misc.Definitions
import silentorb.mythic.physics.Collision

data class Triggering(
    val actor: Id,
    val action: EventTrigger,
    val target: Id,
    val strength: Int? = null
)

typealias EventMapper<ActionType, EventType> = (ActionType, Triggering) -> EventType

inline fun <reified ActionType> gatherMappedEvents(crossinline mapper: EventMapper<ActionType, GameEvent>): (List<Triggering>) -> Events =
    { triggers ->
      triggers.mapNotNull { trigger ->
        val action = trigger.action
        val target = trigger.target
        val actor = trigger.actor
        val strength = trigger.strength
        when (action) {
          is ActionType -> mapper(action, trigger)
          else -> null
        }
      }
    }

val damageEvents = gatherMappedEvents<DamageAction> { action, trigger ->
  DamageEvent(
      damage = Damage(
          type = action.damageType,
          amount = overTime(trigger.strength ?: action.amount),
          source = trigger.actor
      ),
      target = trigger.target
  )
}

val newBuffEvents = gatherMappedEvents<ApplyBuff> { action, trigger ->
  ApplyBuffEvent(
      buffType = action.buffType,
      strength = action.strength,
      duration = action.duration,
      target = trigger.target,
      source = trigger.actor
  )
}

val takeItemEvents = gatherMappedEvents<TakeItem> { action, trigger ->
  TakeItemEvent(
      actor = trigger.actor,
      item = trigger.target
  )
}

typealias ActionHandler<T> = (Definitions, Deck, T, Id, Id) -> DeckSource

typealias TriggerToEventMap = (List<Triggering>) -> Events

fun gatherCommandTriggers(deck: Deck, commands: Commands): List<Triggering> {
  return commands.mapNotNull { command ->
    when (command.type) {
      CommonCharacterCommands.interactPrimary -> {
        val player = deck.players.keys.first()
        val character = deck.characters[player]!!
        val interactable = deck.interactables[character.canInteractWith]
        val action = interactable?.primaryCommand?.action
        if (action != null) {
          Triggering(
              actor = player,
              action = action,
              target = character.canInteractWith!!
          )
        } else {
          null
        }
      }
      else -> null
    }
  }
}

fun gatherActivatedTriggers(deck: Deck, definitions: Definitions, collisions: List<Collision>, commands: Commands): List<Triggering> {
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
  val commandTriggers = gatherCommandTriggers(deck, commands)

  return attachmentTriggers
      .plus(sensorTriggers)
      .plus(buffTriggers)
      .plus(commandTriggers)
}

fun triggersToEvents(triggers: List<Triggering>): Events =
    listOf(
        damageEvents,
        takeItemEvents,
        newBuffEvents
    )
        .flatMap { it(triggers) }

