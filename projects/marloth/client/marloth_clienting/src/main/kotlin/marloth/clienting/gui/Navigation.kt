package marloth.clienting.gui

import marloth.clienting.ClientState
import marloth.clienting.input.GuiCommandType

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

val updateClientCurrentMenus: (ClientState) -> ClientState = { state ->
  val newViews = state.commands
      .mapNotNull { command ->
        val view = nextView(command.type, state.playerViews[command.target] ?: ViewId.none)
        if (view != null)
          Pair(command.target, view)
        else
          null
      }
      .associate { it }

  if (newViews.any()) {
    state.copy(
        playerViews = state.playerViews.plus(newViews)
    )
  } else
    state
}
