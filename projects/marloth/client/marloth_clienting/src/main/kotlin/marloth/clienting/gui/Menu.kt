package marloth.clienting.gui

import haft.HaftCommands
import haft.simpleCommand
import marloth.clienting.CommandType
import marloth.clienting.UserCommand
import marloth.clienting.UserCommands
import mythic.bloom.BloomEvent
import mythic.bloom.LogicModule
import mythic.bloom.StateBag

enum class ViewId {
  mainMenu,
  none
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

//fun possibleToggle(value: Boolean, shouldToggle: Boolean) =
//    if (shouldToggle)
//      !value
//    else
//      value

fun cycle(value: Int, max: Int) = (value + max) % max

//val menuCommandAliases: Map<CommandType, CommandType> = mapOf(
//    CommandType.lookUp to CommandType.moveUp,
//    CommandType.lookDown to CommandType.moveDown,
//    CommandType.lookLeft to CommandType.moveLeft,
//    CommandType.lookRight to CommandType.moveRight
//)

//fun applyMenuSelect(menu: Menu, focusIndex: Int, target: Int): UserCommand {
//  val menuButton = menu[focusIndex]
//  return simpleCommand(menuButton.command, target)
//}

//fun mapMenuCommandAliases(commands: UserCommands): UserCommands {
//  return commands.map { command ->
//    val alias = menuCommandAliases[command.type]
//    if (alias != null)
//      command.copy(type = alias)
//    else
//      command
//  }
//}

//fun mapMenuCommandsToGlobalCommands(menu: Menu, state: MenuState, commands: UserCommands): UserCommands {
//  return commands.mapNotNull { command ->
//    if (command.type == CommandType.menuSelect)
//      applyMenuSelect(menu, state.focusIndex, command.target)
//    else
//      null
//  }
//}

fun menuFocusIndexLogic(menu: Menu): LogicModule = { bundle ->
  val events = bundle.state.input.current.events
  val index = menuFocusIndex(bundle.state.bag)
  val newIndex = when {
    events.contains(BloomEvent.down) -> cycle(index + 1, menu.size)
    events.contains(BloomEvent.up) -> cycle(index - 1, menu.size)
    else -> index
  }
  mapOf(menuFocusIndexKey to newIndex)
}

fun menuNavigationLogic(menu: Menu): LogicModule = { bundle ->
  val events = bundle.state.input.current.events
  val bag = bundle.state.bag
  val view = currentView(bag)
  val activated = events.contains(BloomEvent.activate)
  val newView = if (activated || events.contains(BloomEvent.back))
    ViewId.none
  else
    view

  mapOf(
      currentViewKey to newView
  )
}

fun menuCommandLogic(menu: Menu): LogicModule = { bundle ->
  val events = bundle.state.input.current.events
  val bag = bundle.state.bag
  val activated = events.contains(BloomEvent.activate)
  val commands = if (activated)
    listOf(menu[menuFocusIndex(bag)].command)
  else
    listOf()

  mapOf(
      menuCommandsKey to commands
  )
}

//fun updateMenuState(menu: Menu, state: MenuState, commands: List<BloomEvent>): MenuState {
//  if (commands.isEmpty())
//    return state
//
//  val isActive = haft.isActive(commands)
//
//  val isVisible = possibleToggle(state.isVisible, isActive(CommandType.menu))
//      && !isActive(CommandType.menuSelect) // This line will work until there are sub-menus
//      && !isActive(CommandType.menuBack) // This line will work until there are sub-menus
//
//  if (!isVisible) {
//    state.isVisible = false  // A compromise in the stead of partial immutable updates
//    return state
//  }
//
//  val menuJustAppeared = !state.isVisible
//
//  val focusIndex = if (menuJustAppeared)
//    0
//  else
//    cycle(state.focusIndex + menuFocusMod(isActive), menu.size)
//
//  return state.copy(
//      isVisible = isVisible,
//      focusIndex = focusIndex
//  )
//}
