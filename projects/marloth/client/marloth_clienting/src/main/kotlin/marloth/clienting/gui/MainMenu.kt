package marloth.clienting.gui

import haft.filterKeystrokeCommands
import marloth.clienting.CommandType
import marloth.clienting.UserCommands

fun mainMenu(isGameActive: Boolean): Menu = listOfNotNull(
    if (isGameActive) MenuOption(CommandType.menuBack, Text.continueGame) else null,
    MenuOption(CommandType.newGame, Text.newGame),
    MenuOption(CommandType.quit, Text.quit)
)

//fun updateMenu(state: MenuState, commands: UserCommands, isGameActive: Boolean): Pair<MenuState, UserCommands> {
//  val keystrokeCommands = filterKeystrokeCommands(commands)
//  val menuCommands = mapMenuCommandAliases(keystrokeCommands)
//  val menu = mainMenu(isGameActive)
//  val nextMenuState = updateMenuState(menu, state, menuCommands)
//  val globalCommands = mapMenuCommandsToGlobalCommands(menu, state, menuCommands)
//  return Pair(nextMenuState, globalCommands)
//}