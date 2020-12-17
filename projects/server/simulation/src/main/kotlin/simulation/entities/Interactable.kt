package simulation.entities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import marloth.scenery.enums.ClientCommand
import marloth.scenery.enums.Text
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.EventTrigger
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

typealias InteractableEntry = Map.Entry<Id, Interactable>

fun gatherInteractCommands(deck: Deck, commands: Commands): Commands {
  return deck.players.keys.mapNotNull { player ->
    if (commands.any { it.type == CharacterCommands.interactPrimary && it.target == player }) {
      val interactingWith = deck.characters[player]?.interactingWith
      val interaction = deck.interactables[interactingWith]
      val primaryCommand = interaction?.primaryCommand
      val commandType = primaryCommand?.commandType
      if (commandType != null)
        Command(
            type = commandType,
            target = player,
            value = primaryCommand.commandValue,
        )
      else
        null
    } else
      null
  }
}
