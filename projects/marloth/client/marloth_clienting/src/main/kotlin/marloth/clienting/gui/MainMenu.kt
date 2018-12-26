package marloth.clienting.gui

import marloth.clienting.CommandType

fun mainMenu(isGameActive: Boolean): Menu = listOfNotNull(
    if (isGameActive) MenuOption(CommandType.menuBack, Text.continueGame) else null,
    MenuOption(CommandType.newGame, Text.newGame),
    MenuOption(CommandType.quit, Text.quit)
)