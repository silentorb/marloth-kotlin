package marloth.integration

import haft.HaftCommands
import marloth.clienting.input.GuiCommandType
import mythic.ent.Id
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

fun mapGameCommands(players: List<Id>, commands: HaftCommands): List<Command> =
    commands.mapNotNull { command ->
      val target = players.firstOrNull { it == command.target }
      if (target != null) {
        assert(command.type is CommandType)
        Command(
            type = command.type as CommandType,
            target = target,
            value = command.value
        )
      } else
        null
    }
