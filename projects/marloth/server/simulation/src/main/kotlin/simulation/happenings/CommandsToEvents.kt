package simulation.happenings

import simulation.input.CommandType
import simulation.input.Commands

fun commandsToEvents(commands: Commands): Events =
    commands.mapNotNull { command ->
      val player = command.target
      when (command.type) {

        CommandType.ability -> TryUseAbilityEvent(actor = player)

        else -> null
      }
    }
