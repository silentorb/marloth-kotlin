package marloth.integration

import haft.HaftCommands
import marloth.clienting.input.GuiCommandType
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

//    mapOf(
//    UserCommandType.lookLeft to GuiCommandType.lookLeft,
//    UserCommandType.lookRight to GuiCommandType.lookRight,
//    UserCommandType.lookUp to GuiCommandType.lookUp,
//    UserCommandType.lookDown to GuiCommandType.lookDown
//)

fun mapGameCommands(commands: HaftCommands<CommandType>): List<Command> =
    commands.map {
      Command(
          type = it.type,
          target = it.target.toLong(),
          value = it.value
      )
    }
