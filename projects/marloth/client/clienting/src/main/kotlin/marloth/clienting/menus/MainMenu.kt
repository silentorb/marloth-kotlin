package marloth.clienting.menus

import marloth.clienting.input.GuiCommandType
import marloth.scenery.enums.Text

fun mainMenu(isGameActive: Boolean): List<SimpleMenuItem> = listOfNotNull(
    if (isGameActive) SimpleMenuItem(command = GuiCommandType.menuBack, text = Text.menu_continueGame) else null,
    SimpleMenuItem(command = GuiCommandType.newGame, text = Text.menu_newGame),
    SimpleMenuItem(command = GuiCommandType.quit, text = Text.menu_quit)
)
