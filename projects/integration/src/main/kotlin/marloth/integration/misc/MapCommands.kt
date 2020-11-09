package marloth.integration.misc

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.CommandName

fun mapGameCommands(players: List<Id>, commands: Commands): List<Command> =
    commands
        .filter { command -> command.type is CommandName && players.contains(command.target) }
        .map { command ->
          Command(
              type = command.type as CommandName,
              value = command.value!! as Float,
              target = command.target
          )
        }
