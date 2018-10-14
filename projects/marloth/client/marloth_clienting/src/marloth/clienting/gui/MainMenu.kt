package marloth.clienting.gui

import haft.HaftCommands
import haft.filterKeystrokeCommands
import haft.simpleCommand
import marloth.clienting.CommandType
import marloth.clienting.UserCommand
import marloth.clienting.UserCommands

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

val menuCommands: Map<MenuActionType, CommandType> = mapOf(
    MenuActionType.newGame to CommandType.newGame,
    MenuActionType.continueGame to CommandType.menuBack,
    MenuActionType.quit to CommandType.quit
)

val menuCommandAliases: Map<CommandType, CommandType> = mapOf(
    CommandType.lookUp to CommandType.moveUp,
    CommandType.lookDown to CommandType.moveDown,
    CommandType.lookLeft to CommandType.moveLeft,
    CommandType.lookRight to CommandType.moveRight
)

fun applyMenuSelect(focusIndex: Int, target: Int): UserCommand {
  val menuButton = mainMenu[focusIndex]
  val commandType = menuCommands[menuButton]!!
  return simpleCommand(commandType, target)
}

fun mapMenuCommandAliases(commands: UserCommands): UserCommands {
  return commands.map { command ->
    val alias = menuCommandAliases[command.type]
    if (alias != null)
      command.copy(type = alias)
    else
      command
  }
}

fun mapMenuCommandsToGlobalCommands(state: MenuState, commands: UserCommands): UserCommands {
  return commands.mapNotNull { command ->
    if (command.type == CommandType.menuSelect)
      applyMenuSelect(state.focusIndex, command.target)
    else
      null
  }
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

fun updateMenu(previousState: MenuState, commands: UserCommands): Pair<MenuState, UserCommands> {
  val keystrokeCommands = filterKeystrokeCommands(commands)
  val menuCommands = mapMenuCommandAliases(keystrokeCommands)
  val nextMenuState = updateMenuState(previousState, menuCommands)
  val globalCommands = mapMenuCommandsToGlobalCommands(previousState, menuCommands)
  return Pair(nextMenuState, globalCommands)
}