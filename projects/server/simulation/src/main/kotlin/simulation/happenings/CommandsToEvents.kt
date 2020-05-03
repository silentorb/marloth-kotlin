package simulation.happenings

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import simulation.main.Deck

fun commandsToEvents(deck: Deck, commands: Commands): Events =
    commands.mapNotNull { command ->
      val actor = command.target
      when (command.type) {
        CharacterCommands.ability -> {
          val action = getActiveAction(deck, actor)
          if (action != null) {
            TryUseAbilityEvent(
                actor = actor,
                action = action
            )
          } else
            null
        }
        else -> null
      }
    }
