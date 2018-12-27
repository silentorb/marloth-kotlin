package marloth.integration

import marloth.clienting.UserCommands
import simulation.Command

private typealias UserCommandType = marloth.clienting.CommandType
private typealias CharacterCommandType = simulation.CommandType

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
//    UserCommandType.lookLeft to CommandType.lookLeft,
//    UserCommandType.lookRight to CommandType.lookRight,
//    UserCommandType.lookUp to CommandType.lookUp,
//    UserCommandType.lookDown to CommandType.lookDown
//)

fun mapCommands(players: List<Int>, userCommands: UserCommands): List<Command> =
    userCommands.mapNotNull {
      val type = commandTypeMap[it.type]
      if (type == null)
        null
      else
        Command(
            type = type,
            target = it.target.toLong(),
            value = it.value
        )
    }