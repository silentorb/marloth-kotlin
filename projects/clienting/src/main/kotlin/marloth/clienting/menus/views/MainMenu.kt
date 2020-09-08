package marloth.clienting.menus.views

import marloth.clienting.input.GuiCommandType
import marloth.clienting.menus.*
import marloth.scenery.enums.Text
import simulation.main.World

fun mainMenuItems(isGameActive: Boolean): List<SimpleMenuItem> = listOfNotNull(
    if (isGameActive) SimpleMenuItem(command = GuiCommandType.menuBack, text = Text.menu_continueGame) else null,
    SimpleMenuItem(event = clientEvent(GuiCommandType.newGame), text = Text.menu_newGame),
    SimpleMenuItem(event = clientEvent(GuiCommandType.navigate, ViewId.options), text = Text.gui_optionsMenu),
    SimpleMenuItem(event = clientEvent(GuiCommandType.quit), text = Text.menu_quit)
)

fun mainMenu(world: World?) =
    simpleMenuFlower(Text.gui_mainMenu, mainMenuItems(gameIsActive(world)))
