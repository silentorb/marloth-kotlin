package marloth.integration.misc

import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.CommandName
import silentorb.mythic.happenings.commonCharacterCommands
import silentorb.mythic.ent.Id
import silentorb.mythic.haft.HaftCommands

fun mapGameCommands(players: List<Id>, commands: HaftCommands): List<CharacterCommand> =
    commands
        .filter { command -> commonCharacterCommands.contains(command.type) && players.contains(command.target) }
        .map { command ->
          CharacterCommand(
              type = command.type as CommandName,
              target = command.target,
              value = command.value
          )
        }
