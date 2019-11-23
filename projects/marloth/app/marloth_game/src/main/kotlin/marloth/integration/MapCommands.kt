package marloth.integration

import haft.HaftCommands
import marloth.clienting.input.GuiCommandType
import mythic.ent.Table
import simulation.entities.Player
import simulation.input.Command
import simulation.input.CommandType

private typealias UserCommandType = GuiCommandType
private typealias CharacterCommandType = CommandType

// Associate user commands and child commands that share the same name.
private val commandTypeMap: Map<UserCommandType, CharacterCommandType> =
    CharacterCommandType.values().mapNotNull { type ->
      val other = UserCommandType.values().firstOrNull { it.name == type.name }
      if (other == null)
        null
      else
        Pair(other, type)
    }
        .associate { it }

fun mapGameCommands(players: Table<Player>, commands: HaftCommands<CommandType>): List<Command> =
    commands.map { command ->
      Command(
          type = command.type,
          target = players.entries.first { it.value.playerId == command.target }.key,
          value = command.value
      )
    }
