package simulation.happenings

import silentorb.mythic.commanding.CommonCharacterCommands
import silentorb.mythic.commanding.Commands

fun commandsToEvents(commands: Commands): Events =
    commands.mapNotNull { command ->
      val player = command.target
      when (command.type) {

        CommonCharacterCommands.ability -> TryUseAbilityEvent(actor = player)

        else -> null
      }
    }
