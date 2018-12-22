package marloth.clienting.gui

import haft.HaftCommands
import haft.simpleCommand
import marloth.clienting.CommandType
import marloth.clienting.UserCommand
import marloth.clienting.UserCommands

enum class MenuId {
  main
}

enum class MenuAction {
  none,
  newGame,
  continueGame,
  quit
}

data class MenuOption(
    val command: CommandType,
    val text: Text
)

typealias Menu = List<MenuOption>

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

//val menuCommands: Map<MenuAction, CommandType> = mapOf(
//    MenuAction.continueGame to CommandType.menuBack,
//    MenuAction.newGame to CommandType.newGame,
//    MenuAction.quit to CommandType.quit
//)

val menuCommandAliases: Map<CommandType, CommandType> = mapOf(
    CommandType.lookUp to CommandType.moveUp,
    CommandType.lookDown to CommandType.moveDown,
    CommandType.lookLeft to CommandType.moveLeft,
    CommandType.lookRight to CommandType.moveRight
)

fun applyMenuSelect(menu: Menu, focusIndex: Int, target: Int): UserCommand {
  val menuButton = menu[focusIndex]
  return simpleCommand(menuButton.command, target)
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

fun mapMenuCommandsToGlobalCommands(menu: Menu, state: MenuState, commands: UserCommands): UserCommands {
  return commands.mapNotNull { command ->
    if (command.type == CommandType.menuSelect)
      applyMenuSelect(menu, state.focusIndex, command.target)
    else
      null
  }
}

fun menuFocusMod(isActive: (CommandType) -> Boolean): Int =
    when {
      isActive(CommandType.moveDown) -> 1
      isActive(CommandType.moveUp) -> -1
      else -> 0
    }

fun updateMenuState(menu: Menu, state: MenuState, commands: HaftCommands<CommandType>): MenuState {
  if (commands.isEmpty())
    return state

  val isActive = haft.isActive(commands)

  val isVisible = possibleToggle(state.isVisible, isActive(CommandType.menu))
      && !isActive(CommandType.menuSelect) // This line will work until there are sub-menus
      && !isActive(CommandType.menuBack) // This line will work until there are sub-menus

  if (!isVisible) {
    state.isVisible = false  // A compromise in the stead of partial immutable updates
    return state
  }

  val focusIndex = if (!state.isVisible)
    0 // Menu just appeared
  else
    cycle(state.focusIndex + menuFocusMod(isActive), menu.size)

  return state.copy(
      isVisible = isVisible,
      focusIndex = focusIndex
  )
}

fun newMenuState() = MenuState(
    isVisible = false,
    focusIndex = 0,
    menuId = MenuId.main
)
