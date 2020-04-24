package simulation.happenings

import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events

fun commandsToEvents(commands: Commands): Events =
    commands.mapNotNull { command ->
      val player = command.target
      when (command.type) {
        CharacterCommands.ability -> TryUseAbilityEvent(actor = player)
        else -> null
      }
    }
