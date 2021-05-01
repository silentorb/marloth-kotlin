package simulation.happenings

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.happenings.Commands
import simulation.main.Deck

//data class Triggering<T: Action>(
//    val actor: Id,
//    val action: T,
//    val target: Id,
//    val strength: Int? = null
//)

//fun commandsToTriggers(deck: Deck, commands: Commands): List<Triggering> {
//  return commands.mapNotNull { command ->
//    when (command.type) {
//      CharacterCommands.interactPrimary -> {
//        val player = deck.players.keys.first()
//        val character = deck.characters[player]!!
//        val interactable = deck.interactables[character.canInteractWith]
//        val action = interactable?.primaryCommand?.action
//        if (action != null) {
//          Triggering(
//              actor = player,
//              action = action,
//              target = character.canInteractWith!!
//          )
//        } else {
//          null
//        }
//      }
//      else -> null
//    }
//  }
//}
