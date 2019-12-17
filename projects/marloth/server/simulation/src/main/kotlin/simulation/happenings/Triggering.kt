package simulation.happenings

import silentorb.mythic.happenings.CommonCharacterCommands
import silentorb.mythic.happenings.Commands
import simulation.main.Deck

//data class Triggering<T: Action>(
//    val actor: Id,
//    val action: T,
//    val target: Id,
//    val strength: Int? = null
//)

fun commandsToTriggers(deck: Deck, commands: Commands): List<Triggering> {
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
