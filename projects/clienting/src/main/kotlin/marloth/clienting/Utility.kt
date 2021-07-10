package marloth.clienting

import marloth.clienting.input.GuiCommandType
import silentorb.mythic.happenings.Commands

fun getNewGameCommand(commands: Commands) =
    commands
        .firstOrNull { it.type == GuiCommandType.newGame }
