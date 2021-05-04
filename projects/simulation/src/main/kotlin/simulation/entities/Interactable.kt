package simulation.entities

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.ent.Id
import marloth.scenery.enums.Text
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.main.Deck

//data class WidgetCommand(
//    val text: Text,
//    val commandType: Any? = null,
//)

object Interactables {
  val door = "door"
  val item = "item"
  val bed = "bed"
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

fun getInteractionCommandType(interactionType: String?): String? =
    when (interactionType) {
      Interactables.bed -> Interactions.sleep
      Interactables.item -> Interactions.take
      else -> interactionType
    }

fun gatherInteractionEvents(deck: Deck, commands: Commands): Events {
  return deck.players.keys.mapNotNull { player ->
    if (commands.any { it.type == CharacterCommands.interactPrimary && it.target == player }) {
      val target = deck.characters[player]?.canInteractWith
      val interaction = deck.interactables[target]
      val commandType = getInteractionCommandType(interaction?.type)
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
