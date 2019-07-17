package marloth.clienting

import marloth.clienting.gui.ViewId
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.UserCommands

fun nextView(command: GuiCommandType, view: ViewId): ViewId? =
    when (command) {
      GuiCommandType.menu -> {
        if (view != ViewId.none)
          ViewId.none
        else
          ViewId.mainMenu
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

fun menuChangeView(commands: UserCommands): (ClientState) -> ClientState = { state ->
  val newView = commands
      .mapNotNull { command -> nextView(command.type, state.view) }
      .firstOrNull()

  if (newView != null) {
    state.copy(
        view = newView
    )
  } else
    state
}
