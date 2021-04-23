package simulation.entities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import marloth.scenery.enums.Text
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.EventTrigger
import silentorb.mythic.happenings.Events
import simulation.main.Deck

data class WidgetCommand(
    val text: Text,
    val action: EventTrigger? = null,
    val commandType: Any? = null,
    val commandValue: Any? = null,
)

data class Interactable(
    val primaryCommand: WidgetCommand,
    val secondaryCommand: WidgetCommand? = null
)

private const val interactableMaxDistance = 5f
private const val interactableMaxRotation = 0.99f

data class Interaction(
    val actor: Id,
    val type: String,
    val target: Id,
)

fun gatherInteractionEvents(deck: Deck, commands: Commands): Events {
  return deck.players.keys.mapNotNull { player ->
    if (commands.any { it.type == CharacterCommands.interactPrimary && it.target == player }) {
      val target = deck.characters[player]?.canInteractWith
      val interaction = deck.interactables[target]
      val primaryCommand = interaction?.primaryCommand
      val commandType = primaryCommand?.commandType as? String
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

object Interactions {
  val openClose = "openClose"
  val sleep = "sleep"
  val take = "take"
}
