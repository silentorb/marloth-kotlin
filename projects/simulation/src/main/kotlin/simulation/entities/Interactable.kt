package simulation.entities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.accessorize.getFirstAccessory
import simulation.main.Deck

object InteractionActions {
  val close = "close"
  val open = "open"
  val openClose = "openClose"
  val sleep = "sleep"
  val read = "read"
  val take = "take"
}

object InteractionBehaviors {
  val close = "close"
  val harvest = "harvest"
  val open = "open"
  val openClose = "openClose"
  val sleep = "sleep"
  val devotion = "devotion"
  val take = "take"
}

data class Interactable(
    val action: String,
    val onInteract: String,
)

private const val interactableMaxDistance = 5f

data class Interaction(
    val actor: Id,
    val type: String,
    val target: Id,
)

fun getInteractionCommandType(interactionType: String?, mode: String?): String? =
    when (interactionType) {
      InteractionActions.openClose -> when (mode) {
        DoorMode.open -> InteractionActions.close
        DoorMode.closed -> InteractionActions.open
        else -> null
      }
      else -> interactionType
    }

fun canInteractWith(deck: Deck, item: Id): Boolean {
  val interactable = deck.interactables[item]
  return when (interactable?.onInteract) {
    InteractionBehaviors.harvest -> {
      val accessory = getFirstAccessory(deck.accessories, item)
      accessory?.quantity ?: 0 > 0
    }
    else -> true
  }
}

fun gatherInteractionEvents(deck: Deck, commands: Commands): Events {
  return deck.players.keys.mapNotNull { player ->
    if (commands.any { it.type == CharacterCommands.interactPrimary && it.target == player }) {
      val target = deck.characters[player]?.canInteractWith
      val interaction = deck.interactables[target]
      val commandType = getInteractionCommandType(interaction?.onInteract, deck.primaryModes[target]?.mode)
      if (commandType != null && target != null)
        Interaction(
            type = commandType,
            actor = player,
            target = target,
        )
      else
        null
    } else
      null
  }
}
