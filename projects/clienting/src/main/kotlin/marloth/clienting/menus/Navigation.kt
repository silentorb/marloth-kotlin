package marloth.clienting.menus

import marloth.clienting.ClientState
import marloth.clienting.PlayerViews
import marloth.clienting.input.GuiCommandType
import silentorb.mythic.haft.HaftCommands

fun nextView(command: Any, view: ViewId): ViewId? =
    when (command) {
      GuiCommandType.menu -> {
        if (view != ViewId.none)
          ViewId.none
        else
          ViewId.mainMenu
      }

      GuiCommandType.characterInfo -> {
        if (view == ViewId.characterInfo)
          ViewId.none
        else if (view == ViewId.none)
          ViewId.characterInfo
        else
          null
      }

      GuiCommandType.menuBack -> {
        if (view != ViewId.none)
          ViewId.none
        else
          null
      }

      GuiCommandType.newGame -> ViewId.none

      else -> null
    }

fun updateClientCurrentMenus(commands: HaftCommands, playerViews: PlayerViews): PlayerViews {
  val newViews = commands
      .mapNotNull { command ->
        val view = nextView(command.type, playerViews[command.target] ?: ViewId.none)
        if (view != null)
          Pair(command.target, view)
        else
          null
      }
      .associate { it }

  return if (newViews.any())
    playerViews + newViews
  else
    playerViews
}
