package marloth.clienting.gui

import marloth.clienting.input.GuiCommandType
import scenery.enums.Text

fun mainMenu(isGameActive: Boolean): Menu = listOfNotNull(
    if (isGameActive) MenuOption(GuiCommandType.menuBack, Text.menu_continueGame) else null,
    MenuOption(GuiCommandType.newGame, Text.menu_newGame),
    MenuOption(GuiCommandType.quit, Text.menu_quit)
)
