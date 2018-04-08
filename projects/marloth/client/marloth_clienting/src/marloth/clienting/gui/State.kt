package marloth.clienting.gui

import commanding.CommandType
import haft.Commands

data class MenuState(
    val isVisible: Boolean
)

fun updateMenuState(state: MenuState, commands: Commands<CommandType>): MenuState {
  return state
}

fun initialMenuState() = MenuState(
    isVisible = false
)