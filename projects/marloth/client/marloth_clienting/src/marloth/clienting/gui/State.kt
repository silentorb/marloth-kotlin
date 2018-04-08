package marloth.clienting.gui

import commanding.CommandType
import haft.Commands

data class MenuState(
    val isVisible: Boolean
)

fun possibleToggle(value: Boolean, shouldToggle: Boolean) =
    if (shouldToggle)
      !value
    else
      value

fun updateMenuState(state: MenuState, commands: Commands<CommandType>): MenuState {
  if (commands.size == 0)
    return state

  val isActive = haft.isActive(commands)

  val isVisible = possibleToggle(state.isVisible, isActive(CommandType.menu))

  return MenuState(
      isVisible = isVisible
  )
}

fun initialMenuState() = MenuState(
    isVisible = false
)