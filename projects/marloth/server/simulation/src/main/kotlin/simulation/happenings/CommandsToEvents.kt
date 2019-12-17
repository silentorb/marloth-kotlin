package simulation.happenings

import silentorb.mythic.happenings.CommonCharacterCommands
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events

fun commandsToEvents(commands: Commands): Events =
    commands.mapNotNull { command ->
      val player = command.target
      when (command.type) {
        CommonCharacterCommands.ability -> TryUseAbilityEvent(actor = player)
        else -> null
      }
    }
