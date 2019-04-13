package marloth.clienting.gui

import marloth.clienting.input.GuiCommandType
import scenery.Text

fun mainMenu(isGameActive: Boolean): Menu = listOfNotNull(
    if (isGameActive) MenuOption(GuiCommandType.menuBack, Text.continueGame) else null,
    MenuOption(GuiCommandType.newGame, Text.newGame),
    MenuOption(GuiCommandType.quit, Text.quit)
)