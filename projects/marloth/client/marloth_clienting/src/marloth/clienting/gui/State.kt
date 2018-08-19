package marloth.clienting.gui

import haft.HaftCommands
import marloth.clienting.CommandType

enum class MenuId {
  main
}

enum class MenuActionType {
  none,
  newGame,
  continueGame,
  quit
}

data class MenuState(
    var isVisible: Boolean,
    val focusIndex: Int,
    val menuId: MenuId
)

fun possibleToggle(value: Boolean, shouldToggle: Boolean) =
    if (shouldToggle)
      !value
    else
      value

fun cycle(value: Int, max: Int) = (value + max) % max

val mainMenu = listOf(
    MenuActionType.newGame,
    MenuActionType.continueGame,
    MenuActionType.quit
)

fun menuButtonAction(state: MenuState, commands: HaftCommands<CommandType>): MenuActionType {
  if (haft.isActive(commands, CommandType.menuSelect)) {
    return mainMenu[state.focusIndex]
  }
  return MenuActionType.none
}

fun updateMenuState(state: MenuState, commands: HaftCommands<CommandType>): MenuState {
  if (commands.size == 0)
    return state

  val isActive = haft.isActive(commands)

  val isVisible = possibleToggle(state.isVisible, isActive(CommandType.menu))
      && !isActive(CommandType.menuSelect)// This line will work until there are sub-menus

  if (!isVisible) {
    state.isVisible = false  // A compromise in the stead of partial immutable updates
    return state
  }

  val focusMod = if (isActive(CommandType.moveDown))
    1
  else if (isActive(CommandType.moveUp))
    -1
  else
    0

  val menuSize = 3

  return MenuState(
      isVisible = isVisible,
      focusIndex = cycle(state.focusIndex + focusMod, menuSize),
      menuId = state.menuId
  )
}

fun initialMenuState() = MenuState(
    isVisible = false,
    focusIndex = 0,
    menuId = MenuId.main
)