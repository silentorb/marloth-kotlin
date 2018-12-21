package marloth.clienting.gui

import haft.filterKeystrokeCommands
import marloth.clienting.UserCommands

fun mainMenu(isGameActive: Boolean): Menu = listOfNotNull(
    if (isGameActive) MenuActionType.continueGame else null,
    MenuActionType.newGame,
    MenuActionType.quit
)

fun updateMenu(state: MenuState, commands: UserCommands, isGameActive: Boolean): Pair<MenuState, UserCommands> {
  val keystrokeCommands = filterKeystrokeCommands(commands)
  val menuCommands = mapMenuCommandAliases(keystrokeCommands)
  val menu = mainMenu(isGameActive)
  val nextMenuState = updateMenuState(menu, state, menuCommands)
  val globalCommands = mapMenuCommandsToGlobalCommands(menu, state, menuCommands)
  return Pair(nextMenuState, globalCommands)
}