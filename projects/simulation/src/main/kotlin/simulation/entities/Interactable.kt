package simulation.entities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.main.Deck

object Interactions {
  val close = "close"
  val open = "open"
  val openClose = "openClose"
  val sleep = "sleep"
  val take = "take"
}

data class Interactable(
    val type: String,
)

private const val interactableMaxDistance = 5f

data class Interaction(
    val actor: Id,
    val type: String,
    val target: Id,
)

fun getInteractionCommandType(interactionType: String?, mode: String?): String? =
    when (interactionType) {
      Interactions.openClose -> when(mode) {
        DoorMode.open -> Interactions.close
        DoorMode.closed -> Interactions.open
        else -> null
      }
      else -> interactionType
    }

fun gatherInteractionEvents(deck: Deck, commands: Commands): Events {
  return deck.players.keys.mapNotNull { player ->
    if (commands.any { it.type == CharacterCommands.interactPrimary && it.target == player }) {
      val target = deck.characters[player]?.canInteractWith
      val interaction = deck.interactables[target]
      val commandType = getInteractionCommandType(interaction?.type, deck.primaryModes[target]?.mode)
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
